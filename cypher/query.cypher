// sp_bookRoom
// STEP 1: tìm hoặc tạo customer
MERGE (c:Customer {phone: $phone})
  ON CREATE SET
  c.customerId = $customerId,
  c.fullName = $fullName,
  c.email = $email,
  c.idCard = $idCard,
  c.regisDate = datetime(),
  c.loyaltyPoints = 0
  ON MATCH SET
  c.fullName = coalesce($fullName, c.fullName),
  c.email = coalesce($email, c.email),
  c.idCard = coalesce($idCard, c.idCard)
RETURN c;

// STEP 2: tìm order chưa thanh toán
MATCH (c:Customer {phone: $phone})
OPTIONAL MATCH (o:Order)-[:OF_CUSTOMER]->(c)
  WHERE o.orderStatus = 'Chưa thanh toán'
RETURN o, c
  ORDER BY o.orderDate DESC
  LIMIT 1;

// STEP 3: nếu chưa có order thì tạo mới
MATCH (c:Customer {customerId: $customerId})
MATCH (e:Employee {employeeId: $employeeId})
CREATE (o:Order {
  orderId: $orderId,
  orderDate: datetime(),
  total: 0,
  orderStatus: 'Chưa thanh toán'
})
MERGE (o)-[:OF_CUSTOMER]->(c)
MERGE (o)-[:CREATED_BY]->(e)
RETURN o;


// =========================================================
// sp_RecalcOrderPromotionByLoyalty
MATCH (o:Order {orderId: $orderId})-[:OF_CUSTOMER]->(c:Customer)
WITH o, c,
     CASE
       WHEN c.loyaltyPoints >= 40 THEN 20
       WHEN c.loyaltyPoints >= 20 THEN 15
       WHEN c.loyaltyPoints >= 10 THEN 10
       ELSE 0
       END AS discountPercent
OPTIONAL MATCH (p:Promotion)
  WHERE discountPercent > 0
  AND p.discount = discountPercent
  AND p.startTime <= datetime()
  AND p.endTime >= datetime()
  AND coalesce(p.quantity, 0) > 0
WITH o, p
  ORDER BY p.startTime DESC
WITH o, collect(p)[0] AS selectedPromotion
OPTIONAL MATCH (o)-[old:APPLIES_PROMOTION]->(:Promotion)
DELETE old
FOREACH (_ IN CASE WHEN selectedPromotion IS NULL THEN [] ELSE [1] END |
  MERGE (o)-[:APPLIES_PROMOTION]->(selectedPromotion)
)
RETURN o, selectedPromotion;

// =========================================================
// sp_CancelBooking
MATCH (r:Room {roomId: $roomId})<-[:FOR_ROOM]-(odr:OrderDetailRoom {status:'Đặt'})<-[:HAS_ROOM_DETAIL]-(o:Order)-[:OF_CUSTOMER]->(c:Customer)
WITH r, odr, o, c
DETACH DELETE odr
WITH r, o, c
SET r.isAvailable = true
WITH r, o, c
OPTIONAL MATCH (o)-[:HAS_ROOM_DETAIL]->(remaining:OrderDetailRoom)
WITH r, o, c, count(remaining) AS roomDetailCount
FOREACH (_ IN CASE WHEN roomDetailCount = 0 THEN [1] ELSE [] END |
DETACH DELETE o
)
WITH c
OPTIONAL MATCH (x:Order)-[:OF_CUSTOMER]->(c)
WITH c, count(x) AS orderCount
FOREACH (_ IN CASE WHEN orderCount = 0 THEN [1] ELSE [] END |
DETACH DELETE c
);

// =========================================================
// sp_CheckIn
MATCH (:Room {roomId: $roomId})<-[:FOR_ROOM]-(odr:OrderDetailRoom {status:'Đặt'})
SET odr.status = 'Check-in'
RETURN odr;

// =========================================================
// sp_CheckOut
MATCH (r:Room {roomId: $roomId})<-[:FOR_ROOM]-(odr:OrderDetailRoom {status:'Check-in'})<-[:HAS_ROOM_DETAIL]-(o:Order)-[:OF_CUSTOMER]->(c:Customer)
SET odr.status = 'Hoàn tất',
o.total = coalesce(o.total, 0) + coalesce(odr.roomFee, 0),
c.loyaltyPoints = coalesce(c.loyaltyPoints, 0) + 2,
r.isAvailable = true
RETURN o.orderId AS orderId, c.customerId AS customerId;


// =========================================================
// sp_GiaHanPhong
MATCH (odr:OrderDetailRoom)-[:FOR_ROOM]->(r:Room {roomId:$roomId})
MATCH (r)-[:HAS_TYPE]->(rt:RoomType)
WHERE trim(odr.status) IN ['Check-in', 'Đặt']
WITH odr, rt
ORDER BY odr.orderDetailRoomId DESC
LIMIT 1
SET odr.checkOutDate = datetime($newCheckOutDate),
odr.roomFee = $newRoomFee
RETURN true AS ok, odr;

// =========================================================
// sp_ChangeRoomBeforeCheckIn
MATCH (oldRoom:Room {roomId:$oldRoomId})<-[:FOR_ROOM]-(odr:OrderDetailRoom {status:'Đặt'})<-[:HAS_ROOM_DETAIL]-(o:Order)
MATCH (newRoom:Room {roomId:$newRoomId})
WHERE newRoom.isAvailable = true
DELETE (odr)-[:FOR_ROOM]->(oldRoom)
MERGE (odr)-[:FOR_ROOM]->(newRoom)
SET odr.checkInDate = datetime($newCheckIn),
odr.checkOutDate = datetime($newCheckOut),
odr.roomFee = $newRoomFee,
odr.status = 'Đặt',
oldRoom.isAvailable = true,
newRoom.isAvailable = false
RETURN true AS ok, odr;

// =========================================================
// sp_ChangeRoomWhileCheckIn
MATCH (oldRoom:Room {roomId:$oldRoomId})<-[:FOR_ROOM]-(oldOdr:OrderDetailRoom {status:'Check-in'})<-[:HAS_ROOM_DETAIL]-(o:Order)
MATCH (newRoom:Room {roomId:$newRoomId})
WHERE newRoom.isAvailable = true
SET oldOdr.checkOutDate = datetime($changeTime),
oldOdr.roomFee = $oldFee
CREATE (newOdr:OrderDetailRoom {
orderDetailRoomId: $newOrderDetailRoomId,
roomFee: $newFee,
bookingDate: oldOdr.bookingDate,
checkInDate: datetime($changeTime),
checkOutDate: oldOdr.checkOutDate,
bookingType: oldOdr.bookingType,
status: 'Check-in'
})
MERGE (o)-[:HAS_ROOM_DETAIL]->(newOdr)
MERGE (newOdr)-[:FOR_ROOM]->(newRoom)
SET newRoom.isAvailable = false
RETURN true AS ok, oldOdr, newOdr;

// =========================================================
// sp_AddServiceToRoom
MATCH (r:Room {roomId:$roomId})<-[:FOR_ROOM]-(odr:OrderDetailRoom {status:'Check-in'})<-[:HAS_ROOM_DETAIL]-(o:Order)
MATCH (s:Service {serviceName:$serviceName})
WHERE coalesce(s.quantity, 0) >= $quantity
CREATE (ods:OrderDetailService {
orderDetailId: $orderDetailId,
quantity: $quantity,
serviceFee: $serviceFee
})
MERGE (o)-[:HAS_SERVICE_DETAIL]->(ods)
MERGE (ods)-[:FOR_SERVICE]->(s)
MERGE (ods)-[:USED_IN_ROOM]->(r)
SET o.total = coalesce(o.total, 0) + $serviceFee,
s.quantity = coalesce(s.quantity, 0) - $quantity
RETURN ods, o, s;


// =========================================================
// sp_PayOrder
MATCH (o:Order {orderId:$orderId})
WHERE o.orderStatus = 'Chưa thanh toán'
SET o.orderDate = datetime(),
o.orderStatus = 'Thanh toán'
WITH o
OPTIONAL MATCH (o)-[:HAS_ROOM_DETAIL]->(odr:OrderDetailRoom)-[:FOR_ROOM]->(r:Room)
SET odr.status = 'Hoàn tất',
r.isAvailable = true
WITH DISTINCT o
OPTIONAL MATCH (o)-[:APPLIES_PROMOTION]->(p:Promotion)
WITH o, p, coalesce(o.total, 0) AS subTotal, coalesce(p.discount, 0) AS discount
WITH o, p, subTotal * (1 - discount / 100.0) * 1.10 AS finalTotal
SET o.total = finalTotal
WITH o, p
FOREACH (_ IN CASE WHEN p IS NULL THEN [] ELSE [1] END |
SET p.quantity =
CASE
WHEN p.quantity IS NULL THEN NULL
WHEN p.quantity > 0 THEN p.quantity - 1
ELSE 0
END
)
RETURN o, p;

// Trả dữ liệu in hóa đơn
MATCH (o:Order {orderId:$orderId})
OPTIONAL MATCH (o)-[:HAS_ROOM_DETAIL]->(odr:OrderDetailRoom)-[:FOR_ROOM]->(r:Room)-[:HAS_TYPE]->(rt:RoomType)
OPTIONAL MATCH (o)-[:HAS_SERVICE_DETAIL]->(ods:OrderDetailService)-[:USED_IN_ROOM]->(r)
OPTIONAL MATCH (ods)-[:FOR_SERVICE]->(s:Service)
RETURN o.orderId AS orderId,
o.orderDate AS orderDate,
o.total AS total,
o.orderStatus AS orderStatus,
r.description AS roomDescription,
rt.roomTypeId AS roomTypeId,
odr.bookingDate AS bookingDate,
odr.checkInDate AS checkInDate,
odr.checkOutDate AS checkOutDate,
odr.bookingType AS bookingType,
s.serviceName AS serviceName,
sum(coalesce(ods.quantity, 0)) AS serviceQuantity
ORDER BY r.roomId, serviceName;

// doanh thu theo ngày
MATCH (o:Order)
WHERE date(o.orderDate) = date($date)
AND o.orderStatus = 'Thanh toán'
RETURN count(o) AS soLuongHoaDon,
sum(coalesce(o.total, 0)) AS totalRevenue;

// daily detail
MATCH (o:Order)
WHERE date(o.orderDate) = date($date)
AND o.orderStatus = 'Thanh toán'
WITH collect(o) AS paid
CALL {
WITH paid
UNWIND paid AS o
RETURN count(o) AS soLuongHoaDon,
sum(coalesce(o.total,0)) AS totalRevenue
}
CALL {
WITH paid
UNWIND paid AS o
OPTIONAL MATCH (o)-[:HAS_ROOM_DETAIL]->(odr:OrderDetailRoom)
RETURN count(odr) AS totalBookings,
sum(coalesce(odr.roomFee,0)) AS roomRevenue
}
CALL {
WITH paid
UNWIND paid AS o
OPTIONAL MATCH (o)-[:HAS_SERVICE_DETAIL]->(ods:OrderDetailService)
RETURN sum(coalesce(ods.quantity,0)) AS totalServiceQty,
sum(coalesce(ods.serviceFee,0)) AS serviceRevenue
}
RETURN soLuongHoaDon, totalBookings, totalServiceQty, roomRevenue, serviceRevenue, totalRevenue;

// revenue range detail
MATCH (o:Order)
WHERE o.orderStatus = 'Thanh toán'
AND o.orderDate >= datetime($start)
AND o.orderDate <= datetime($end)
WITH collect(o) AS paid
CALL {
WITH paid
UNWIND paid AS o
RETURN count(o) AS soLuongHoaDon,
sum(coalesce(o.total,0)) AS totalRevenue
}
CALL {
WITH paid
UNWIND paid AS o
OPTIONAL MATCH (o)-[:HAS_ROOM_DETAIL]->(odr:OrderDetailRoom)
RETURN count(odr) AS tongSoLuotDatPhong,
sum(coalesce(odr.roomFee,0)) AS roomRevenue
}
CALL {
WITH paid
UNWIND paid AS o
OPTIONAL MATCH (o)-[:HAS_SERVICE_DETAIL]->(ods:OrderDetailService)
RETURN sum(coalesce(ods.quantity,0)) AS tongSoDichVu,
sum(coalesce(ods.serviceFee,0)) AS serviceRevenue
}
RETURN soLuongHoaDon, tongSoLuotDatPhong, tongSoDichVu, roomRevenue, serviceRevenue, totalRevenue;

// daily service stats
MATCH (o:Order)-[:HAS_SERVICE_DETAIL]->(ods:OrderDetailService)-[:FOR_SERVICE]->(s:Service)
WHERE date(o.orderDate) = date($date)
AND o.orderStatus = 'Thanh toán'
RETURN s.serviceName AS serviceName,
sum(coalesce(ods.quantity,0)) AS totalQuantity,
sum(coalesce(ods.serviceFee,0)) AS totalRevenue;

// booking type revenue stats
MATCH (o:Order)-[:HAS_ROOM_DETAIL]->(odr:OrderDetailRoom)
WHERE o.orderStatus = 'Thanh toán'
AND o.orderDate >= datetime($start)
AND o.orderDate <= datetime($end)
RETURN odr.bookingType AS bookingType,
count(odr) AS soLuot,
sum(coalesce(odr.roomFee,0)) AS roomRevenue
ORDER BY roomRevenue DESC;

// occupancy rate
MATCH (r:Room)
WITH count(r) AS totalRooms
MATCH (odr:OrderDetailRoom)
WHERE odr.checkOutDate IS NOT NULL
AND datetime($date) >= odr.checkInDate
AND datetime($date) <= odr.checkOutDate
WITH totalRooms, count(odr) AS occupiedRooms
RETURN CASE
WHEN totalRooms = 0 THEN 0
ELSE toFloat(occupiedRooms) / totalRooms * 100
END AS occupancyRate;

// customer return stats
MATCH (c:Customer {customerId:$customerId})<-[:OF_CUSTOMER]-(o:Order)-[:HAS_ROOM_DETAIL]->(odr:OrderDetailRoom)
WHERE odr.checkOutDate IS NOT NULL
RETURN count(odr) AS returnCount;