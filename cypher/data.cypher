// =========================
// UNIQUENESS CONSTRAINTS
// =========================
CREATE CONSTRAINT employee_type_id IF NOT EXISTS
FOR (n:EmployeeType) REQUIRE n.typeId IS UNIQUE;

CREATE CONSTRAINT employee_id IF NOT EXISTS
FOR (n:Employee) REQUIRE n.employeeId IS UNIQUE;

CREATE CONSTRAINT employee_phone IF NOT EXISTS
FOR (n:Employee) REQUIRE n.phone IS UNIQUE;

CREATE CONSTRAINT employee_email IF NOT EXISTS
FOR (n:Employee) REQUIRE n.email IS UNIQUE;

CREATE CONSTRAINT account_id IF NOT EXISTS
FOR (n:Account) REQUIRE n.accountId IS UNIQUE;

CREATE CONSTRAINT account_username IF NOT EXISTS
FOR (n:Account) REQUIRE n.username IS UNIQUE;

CREATE CONSTRAINT customer_id IF NOT EXISTS
FOR (n:Customer) REQUIRE n.customerId IS UNIQUE;

CREATE CONSTRAINT customer_phone IF NOT EXISTS
FOR (n:Customer) REQUIRE n.phone IS UNIQUE;

CREATE CONSTRAINT promotion_id IF NOT EXISTS
FOR (n:Promotion) REQUIRE n.promotionId IS UNIQUE;

CREATE CONSTRAINT room_type_id IF NOT EXISTS
FOR (n:RoomType) REQUIRE n.roomTypeId IS UNIQUE;

CREATE CONSTRAINT room_id IF NOT EXISTS
FOR (n:Room) REQUIRE n.roomId IS UNIQUE;

CREATE CONSTRAINT unit_price_id IF NOT EXISTS
FOR (n:UnitPrice) REQUIRE n.unitId IS UNIQUE;

CREATE CONSTRAINT service_id IF NOT EXISTS
FOR (n:Service) REQUIRE n.serviceId IS UNIQUE;

CREATE CONSTRAINT order_id IF NOT EXISTS
FOR (n:Order) REQUIRE n.orderId IS UNIQUE;

CREATE CONSTRAINT odr_id IF NOT EXISTS
FOR (n:OrderDetailRoom) REQUIRE n.orderDetailRoomId IS UNIQUE;

CREATE CONSTRAINT ods_id IF NOT EXISTS
FOR (n:OrderDetailService) REQUIRE n.orderDetailId IS UNIQUE;

CREATE CONSTRAINT dashboard_note_key IF NOT EXISTS
FOR (n:DashboardNote) REQUIRE n.noteKey IS UNIQUE;

CREATE CONSTRAINT counter_name IF NOT EXISTS
FOR (n:Counter) REQUIRE n.name IS UNIQUE;

// SEARCH INDEXES
CREATE INDEX room_available_idx IF NOT EXISTS
FOR (n:Room) ON (n.isAvailable);

CREATE INDEX order_status_idx IF NOT EXISTS
FOR (n:Order) ON (n.orderStatus);

CREATE INDEX odr_status_idx IF NOT EXISTS
FOR (n:OrderDetailRoom) ON (n.status);

CREATE INDEX promotion_discount_idx IF NOT EXISTS
FOR (n:Promotion) ON (n.discount);

// Counter - Sequence
MERGE (c1:Counter {name: 'EMPLOYEE_SEQ'}) ON CREATE SET c1.value = 0;
MERGE (c2:Counter {name: 'ACCOUNT_SEQ'}) ON CREATE SET c2.value = 0;
MERGE (c3:Counter {name: 'CUSTOMER_SEQ'}) ON CREATE SET c3.value = 0;
MERGE (c4:Counter {name: 'PROMOTION_SEQ'}) ON CREATE SET c4.value = 0;
MERGE (c5:Counter {name: 'ROOMTYPE_SEQ'}) ON CREATE SET c5.value = 0;
MERGE (c6:Counter {name: 'ROOM_SEQ'}) ON CREATE SET c6.value = 0;
MERGE (c7:Counter {name: 'SERVICE_SEQ'}) ON CREATE SET c7.value = 0;
MERGE (c8:Counter {name: 'ORDER_SEQ'}) ON CREATE SET c8.value = 0;
MERGE (c9:Counter {name: 'ODR_SEQ'}) ON CREATE SET c9.value = 0;
MERGE (c10:Counter {name: 'ODS_SEQ'}) ON CREATE SET c10.value = 0;
MERGE (c11:Counter {name: 'UNITPRICE_SEQ'}) ON CREATE SET c11.value = 0;

// ============================================================
// 0) DASHBOARD NOTE
// ============================================================
MERGE (n1:DashboardNote {noteKey: 'DASHBOARD_PLAN'})
SET n1.content = '';

MERGE (n2:DashboardNote {noteKey: 'DASHBOARD_ALERT'})
SET n2.content = '';

// ============================================================
// 1) EMPLOYEE TYPE
// ============================================================
MERGE (et1:EmployeeType {typeId: 'ET0001'})
SET et1.typeName = 'Lễ tân';

MERGE (et2:EmployeeType {typeId: 'ET0002'})
SET et2.typeName = 'Quản lý';

// ============================================================
// 2) EMPLOYEE
// ============================================================
MATCH (et1:EmployeeType {typeId: 'ET0001'})
MATCH (et2:EmployeeType {typeId: 'ET0002'})

MERGE (e1:Employee {employeeId: 'LTBD01'})
SET e1.fullName = 'Bao Dinh',
    e1.phone = '0123456789',
    e1.email = 'bao.dinh@example.com'
MERGE (e1)-[:BELONGS_TO]->(et1)

MERGE (e2:Employee {employeeId: 'QLST02'})
SET e2.fullName = 'Son Tung',
    e2.phone = '0987654321',
    e2.email = 'baodinh.nguyen321@gmail.com',
    e2.imgSource = 'images/fox_profile.png',
    e2.gender = true
MERGE (e2)-[:BELONGS_TO]->(et2)

MERGE (e3:Employee {employeeId: 'LTQH03'})
SET e3.fullName = 'Quang Hung',
    e3.phone = '0905112233',
    e3.email = 'quanghungmasterd@mimosahotel.com'
MERGE (e3)-[:BELONGS_TO]->(et1)

MERGE (e4:Employee {employeeId: 'QLNH04'})
SET e4.fullName = 'Ngoc Hai',
    e4.phone = '0934556677',
    e4.email = 'hai.hn@mimosahotel.com'
MERGE (e4)-[:BELONGS_TO]->(et2)

MERGE (e5:Employee {employeeId: 'QLTA05'})
SET e5.fullName = 'Thien An',
    e5.phone = '0121456789',
    e5.email = 'dtann@mimosahotel.com',
    e5.imgSource = 'images/fox_profile.png',
    e5.gender = true
MERGE (e5)-[:BELONGS_TO]->(et2)

MERGE (e6:Employee {employeeId: 'QLNO06'})
SET e6.fullName = 'Ngoc Oanh',
    e6.phone = '0123453211',
    e6.email = 'oanh.tr@mimosahotel.com'
MERGE (e6)-[:BELONGS_TO]->(et2);

// ============================================================
// 3) ACCOUNT
// ============================================================
MATCH (e1:Employee {employeeId: 'LTBD01'})
MATCH (e2:Employee {employeeId: 'QLST02'})
MATCH (e3:Employee {employeeId: 'LTQH03'})
MATCH (e4:Employee {employeeId: 'QLNH04'})
MATCH (e5:Employee {employeeId: 'QLTA05'})
MATCH (e6:Employee {employeeId: 'QLNO06'})

MERGE (a1:Account {accountId: 'Acc001'})
SET a1.username = 'ltbd01',
    a1.password = '123'
MERGE (e1)-[:HAS_ACCOUNT]->(a1)

MERGE (a2:Account {accountId: 'Acc002'})
SET a2.username = 'qlst02',
    a2.password = '123'
MERGE (e2)-[:HAS_ACCOUNT]->(a2)

MERGE (a3:Account {accountId: 'Acc003'})
SET a3.username = 'ltqh03',
    a3.password = '123'
MERGE (e3)-[:HAS_ACCOUNT]->(a3)

MERGE (a4:Account {accountId: 'Acc004'})
SET a4.username = 'qlnh04',
    a4.password = '123'
MERGE (e4)-[:HAS_ACCOUNT]->(a4)

MERGE (a5:Account {accountId: 'Acc005'})
SET a5.username = 'qlta05',
    a5.password = '123'
MERGE (e5)-[:HAS_ACCOUNT]->(a5)

MERGE (a6:Account {accountId: 'Acc006'})
SET a6.username = 'qlno06',
    a6.password = '123'
MERGE (e6)-[:HAS_ACCOUNT]->(a6);

// ============================================================
// 4) ROOM TYPE
// ============================================================
MERGE (rt1:RoomType {roomTypeId: 'RT0001'})
SET rt1.typeName = 'Phòng đơn',
    rt1.pricePerHour = 60000,
    rt1.pricePerNight = 150000,
    rt1.pricePerDay = 200000,
    rt1.lateFeePerHour = 20000,
    rt1.maxAdults = 2,
    rt1.maxChildren = 1,
    rt1.description = '1 giưỡng đôi rất lớn';

MERGE (rt2:RoomType {roomTypeId: 'RT0002'})
SET rt2.typeName = 'Phòng đôi',
    rt2.pricePerHour = 70000,
    rt2.pricePerNight = 200000,
    rt2.pricePerDay = 300000,
    rt2.lateFeePerHour = 30000,
    rt2.maxAdults = 4,
    rt2.maxChildren = 2,
    rt2.description = '1 giường đôi rất lớn và 1 giường đôi lớn';

// ============================================================
// 5) UNIT PRICE
// ============================================================
MERGE (u1:UnitPrice {unitId: 'UP0001'})
SET u1.unitName = 'lon',
    u1.description = 'Đơn vị tính cho nước ngọt, nước uống đóng lon';

MERGE (u2:UnitPrice {unitId: 'UP0002'})
SET u2.unitName = 'thùng',
    u2.description = 'Đơn vị tính cho bia (1 thùng)';

MERGE (u3:UnitPrice {unitId: 'UP0003'})
SET u3.unitName = 'phần',
    u3.description = 'Đơn vị tính cho món ăn';

MERGE (u4:UnitPrice {unitId: 'UP0004'})
SET u4.unitName = 'lượt',
    u4.description = 'Đơn vị tính cho dịch vụ giặt giũ';

MERGE (u5:UnitPrice {unitId: 'UP0005'})
SET u5.unitName = 'chai',
    u5.description = 'Đơn vị tính cho rượu, nước uống đóng chai';

MERGE (u6:UnitPrice {unitId: 'UP0006'})
SET u6.unitName = 'cái',
    u6.description = 'Đơn vị tính cho dịch vụ bảo vệ sức khỏe';


// ============================================================
// 6) SERVICE
// ============================================================
MATCH (u1:UnitPrice {unitId: 'UP0001'})
MATCH (u2:UnitPrice {unitId: 'UP0002'})
MATCH (u3:UnitPrice {unitId: 'UP0003'})
MATCH (u4:UnitPrice {unitId: 'UP0004'})
MATCH (u5:UnitPrice {unitId: 'UP0005'})
MATCH (u6:UnitPrice {unitId: 'UP0006'})

MERGE (s1:Service {serviceId: 'Serv01'})
SET s1.serviceName = 'Nước ngọt',
s1.price = 15000,
s1.quantity = 10,
s1.serviceType = 'Drink',
s1.imgSource = 'images/pepsi.png'
MERGE (s1)-[:HAS_UNIT]->(u1)

MERGE (s2:Service {serviceId: 'Serv02'})
SET s2.serviceName = 'Nước uống đóng chai',
s2.price = 15000,
s2.quantity = 20,
s2.serviceType = 'Drink',
s2.imgSource = 'images/aquafina.jpg'
MERGE (s2)-[:HAS_UNIT]->(u5)

MERGE (s3:Service {serviceId: 'Serv03'})
SET s3.serviceName = 'Mì tôm',
s3.price = 15000,
s3.quantity = 30,
s3.serviceType = 'Food',
s3.imgSource = 'images/mitom.jpg'
MERGE (s3)-[:HAS_UNIT]->(u3)

MERGE (s4:Service {serviceId: 'Serv04'})
SET s4.serviceName = 'Cocacola',
s4.price = 15000,
s4.quantity = 10,
s4.serviceType = 'Drink',
s4.imgSource = 'images/cocacola.jpg'
MERGE (s4)-[:HAS_UNIT]->(u1)

MERGE (s5:Service {serviceId: 'Serv05'})
SET s5.serviceName = 'Bia Tiger',
s5.price = 20000,
s5.quantity = 60,
s5.serviceType = 'Drink',
s5.imgSource = 'images/biatiger.jpg'
MERGE (s5)-[:HAS_UNIT]->(u2)

MERGE (s6:Service {serviceId: 'Serv06'})
SET s6.serviceName = '7 up',
s6.price = 20000,
s6.quantity = 60,
s6.serviceType = 'Drink',
s6.imgSource = 'images/7up.png'
MERGE (s6)-[:HAS_UNIT]->(u1)

MERGE (s7:Service {serviceId: 'Serv07'})
SET s7.serviceName = 'Cơm bò xào',
s7.price = 30000,
s7.quantity = 60,
s7.serviceType = 'Food',
s7.imgSource = 'images/comboxao.jpg'
MERGE (s7)-[:HAS_UNIT]->(u3)

MERGE (s8:Service {serviceId: 'Serv08'})
SET s8.serviceName = 'Mì ý',
s8.price = 30000,
s8.quantity = 60,
s8.serviceType = 'Food',
s8.imgSource = 'images/miy.jpg'
MERGE (s8)-[:HAS_UNIT]->(u3)

MERGE (s9:Service {serviceId: 'Serv09'})
SET s9.serviceName = 'Nước ép táo',
s9.price = 20000,
s9.quantity = 60,
s9.serviceType = 'Drink',
s9.imgSource = 'images/nuoceptao.png'
MERGE (s9)-[:HAS_UNIT]->(u1)

MERGE (s10:Service {serviceId: 'Serv10'})
SET s10.serviceName = 'Redbull',
s10.price = 20000,
s10.quantity = 60,
s10.serviceType = 'Drink',
s10.imgSource = 'images/redbull.png'
MERGE (s10)-[:HAS_UNIT]->(u1)

MERGE (s11:Service {serviceId: 'Serv11'})
SET s11.serviceName = 'Rượu vang',
s11.price = 20000,
s11.quantity = 60,
s11.serviceType = 'Drink',
s11.imgSource = 'images/ruouvang.png'
MERGE (s11)-[:HAS_UNIT]->(u5)

MERGE (s12:Service {serviceId: 'Serv12'})
SET s12.serviceName = 'Rượu Soju',
s12.price = 20000,
s12.quantity = 60,
s12.serviceType = 'Drink',
s12.imgSource = 'images/soju.png'
MERGE (s12)-[:HAS_UNIT]->(u5)

MERGE (s13:Service {serviceId: 'Serv13'})
SET s13.serviceName = 'Sấy khô',
s13.price = 20000,
s13.quantity = 999,
s13.serviceType = 'Laundry',
s13.imgSource = 'images/saykho.jpg'
MERGE (s13)-[:HAS_UNIT]->(u4)

MERGE (s14:Service {serviceId: 'Serv14'})
SET s14.serviceName = 'Dọn phòng',
s14.price = 20000,
s14.quantity = 999,
s14.serviceType = 'Laundry',
s14.imgSource = 'images/donphong.jpg'
MERGE (s14)-[:HAS_UNIT]->(u4)

MERGE (s15:Service {serviceId: 'Serv15'})
SET s15.serviceName = 'Bảo vệ sức khỏe',
s15.price = 50000,
s15.quantity = 200,
s15.serviceType = 'Health',
s15.imgSource = 'images/panadol.jpg'
MERGE (s15)-[:HAS_UNIT]->(u6);


// ============================================================
// 7) ROOM
// Room01 -> Room20 theo đúng thứ tự INSERT SQL
// ============================================================
MATCH (rt1:RoomType {roomTypeId: 'RT0001'})
MATCH (rt2:RoomType {roomTypeId: 'RT0002'})

MERGE (r1:Room {roomId: 'Room01'})
SET r1.description = 'Phòng 101 - View vườn, gần sảnh',
    r1.isAvailable = true,
    r1.imgRoomSource = 'images/1.jpg',
    r1.view = 'Vườn'
MERGE (r1)-[:HAS_TYPE]->(rt1)

MERGE (r2:Room {roomId: 'Room02'})
SET r2.description = 'Phòng 102 - View thành phố',
    r2.isAvailable = true,
    r2.imgRoomSource = '/images/image031.jpg',
    r2.view = 'Thành phố'
MERGE (r2)-[:HAS_TYPE]->(rt1)

MERGE (r3:Room {roomId: 'Room03'})
SET r3.description = 'Phòng 103 - View vườn, cuối hành lang',
    r3.isAvailable = true,
    r3.imgRoomSource = '/images/image034.jpg',
    r3.view = 'Vườn'
MERGE (r3)-[:HAS_TYPE]->(rt1)

MERGE (r4:Room {roomId: 'Room04'})
SET r4.description = 'Phòng 201 - View ban công, gần sảnh',
    r4.isAvailable = true,
    r4.imgRoomSource = '/images/image029.jpg',
    r4.view = 'Ban công'
MERGE (r4)-[:HAS_TYPE]->(rt2)

MERGE (r5:Room {roomId: 'Room05'})
SET r5.description = 'Phòng 202 - View biển, gần sảnh',
    r5.isAvailable = true,
    r5.imgRoomSource = 'images/5.jpg',
    r5.view = 'Biển'
MERGE (r5)-[:HAS_TYPE]->(rt2)

MERGE (r6:Room {roomId: 'Room06'})
SET r6.description = 'Phòng 203 - View thành phố, gần thang máy',
    r6.isAvailable = true,
    r6.imgRoomSource = 'images/6.jpg',
    r6.view = 'Thành phố'
MERGE (r6)-[:HAS_TYPE]->(rt2)

MERGE (r7:Room {roomId: 'Room07'})
SET r7.description = 'Phòng 300 - View thành phố,  thoáng mát',
    r7.isAvailable = true,
    r7.imgRoomSource = '/images/image016.jpg',
    r7.view = 'Thành phố'
MERGE (r7)-[:HAS_TYPE]->(rt1)

MERGE (r8:Room {roomId: 'Room08'})
SET r8.description = 'Phòng 301 - View thành phố,  thoáng mát',
    r8.isAvailable = true,
    r8.imgRoomSource = '/images/image017.jpg',
    r8.view = 'Thành phố'
MERGE (r8)-[:HAS_TYPE]->(rt1)

MERGE (r9:Room {roomId: 'Room09'})
SET r9.description = 'Phòng 302 - View ban công,  thoáng mát',
    r9.isAvailable = true,
    r9.imgRoomSource = '/images/image019.jpg',
    r9.view = 'Ban công'
MERGE (r9)-[:HAS_TYPE]->(rt2)

MERGE (r10:Room {roomId: 'Room10'})
SET r10.description = 'Phòng 303 - View ban công,  thoáng mát',
    r10.isAvailable = true,
    r10.imgRoomSource = '/images/image020.jpg',
    r10.view = 'Ban công'
MERGE (r10)-[:HAS_TYPE]->(rt1)

MERGE (r11:Room {roomId: 'Room11'})
SET r11.description = 'Phòng 304 - View biển,  mát mẻ',
    r11.isAvailable = true,
    r11.imgRoomSource = '/images/image021.jpg',
    r11.view = 'Biển'
MERGE (r11)-[:HAS_TYPE]->(rt1)

MERGE (r12:Room {roomId: 'Room12'})
SET r12.description = 'Phòng 305 - View biển, rừng thông',
    r12.isAvailable = true,
    r12.imgRoomSource = '/images/image022.jpg',
    r12.view = 'Biển'
MERGE (r12)-[:HAS_TYPE]->(rt2)

MERGE (r13:Room {roomId: 'Room13'})
SET r13.description = 'Phòng 306 - View ban công, mát mẻ',
    r13.isAvailable = true,
    r13.imgRoomSource = '/images/5.jpg',
    r13.view = 'Ban công'
MERGE (r13)-[:HAS_TYPE]->(rt1)

MERGE (r14:Room {roomId: 'Room14'})
SET r14.description = 'Phòng 501 - View ban công, rừng thông',
    r14.isAvailable = true,
    r14.imgRoomSource = '/images/image028.jpg',
    r14.view = 'Ban công'
MERGE (r14)-[:HAS_TYPE]->(rt2)

MERGE (r15:Room {roomId: 'Room15'})
SET r15.description = 'Phòng 502 - View thành phố, đông đúc',
    r15.isAvailable = true,
    r15.imgRoomSource = '/images/image031.jpg',
    r15.view = 'Thành phố'
MERGE (r15)-[:HAS_TYPE]->(rt2)

MERGE (r16:Room {roomId: 'Room16'})
SET r16.description = 'Phòng 503 - View thành phố, đông đúc',
    r16.isAvailable = true,
    r16.imgRoomSource = '/images/image032.jpg',
    r16.view = 'Thành phố'
MERGE (r16)-[:HAS_TYPE]->(rt2)

MERGE (r17:Room {roomId: 'Room17'})
SET r17.description = 'Phòng 601 - View vườn, đông đúc',
    r17.isAvailable = true,
    r17.imgRoomSource = '/images/image033.jpg',
    r17.view = 'Vườn'
MERGE (r17)-[:HAS_TYPE]->(rt1)

MERGE (r18:Room {roomId: 'Room18'})
SET r18.description = 'Phòng 602 - View vườn, đông đúc',
    r18.isAvailable = true,
    r18.imgRoomSource = '/images/image029.jpg',
    r18.view = 'Vườn'
MERGE (r18)-[:HAS_TYPE]->(rt2)

MERGE (r19:Room {roomId: 'Room19'})
SET r19.description = 'Phòng 603 - View ban công, đông đúc',
    r19.isAvailable = true,
    r19.imgRoomSource = '/images/image030.jpg',
    r19.view = 'Ban công'
MERGE (r19)-[:HAS_TYPE]->(rt2)

MERGE (r20:Room {roomId: 'Room20'})
SET r20.description = 'Phòng 604 - View biển, mát mẻ',
    r20.isAvailable = true,
    r20.imgRoomSource = '/images/image034.jpg',
    r20.view = 'Biển'
MERGE (r20)-[:HAS_TYPE]->(rt2);

// ============================================================
// 8) PROMOTION
// ============================================================
MERGE (p1:Promotion {promotionId: 'Promo01'})
SET p1.promotionName = 'Ưu đãi Bạc 10%',
    p1.discount = 10.0,
    p1.startTime = datetime('2025-01-01T00:00:00'),
    p1.endTime = datetime('2026-12-31T00:00:00'),
    p1.quantity = 100;

MERGE (p2:Promotion {promotionId: 'Promo02'})
SET p2.promotionName = 'Ưu đãi Vàng 15%',
    p2.discount = 15.0,
    p2.startTime = datetime('2025-01-01T00:00:00'),
    p2.endTime = datetime('2026-12-31T00:00:00'),
    p2.quantity = 100;

MERGE (p3:Promotion {promotionId: 'Promo03'})
SET p3.promotionName = 'Ưu đãi Kim Cương 20%',
    p3.discount = 20.0,
    p3.startTime = datetime('2025-01-01T00:00:00'),
    p3.endTime = datetime('2026-12-31T00:00:00'),
    p3.quantity = 100;

MERGE (p4:Promotion {promotionId: 'Promo04'})
SET p4.promotionName = 'Ưu đãi Quốc tế thiếu nhi 30%',
    p4.discount = 30.0,
    p4.startTime = datetime('2025-01-01T00:00:00'),
    p4.endTime = datetime('2026-12-31T00:00:00'),
    p4.quantity = 100;

MERGE (p5:Promotion {promotionId: 'Promo05'})
SET p5.promotionName = 'Ưu đãi Valentine 70%',
    p5.discount = 70.0,
    p5.startTime = datetime('2025-01-01T00:00:00'),
    p5.endTime = datetime('2026-12-31T00:00:00'),
    p5.quantity = 100;


// ============================================================
// 9) SYNC COUNTERS
// ============================================================
MATCH (c:Counter {name:'EMPLOYEE_SEQ'})  SET c.value = 6;
MATCH (c:Counter {name:'ACCOUNT_SEQ'})   SET c.value = 6;
MATCH (c:Counter {name:'CUSTOMER_SEQ'})  SET c.value = 0;
MATCH (c:Counter {name:'PROMOTION_SEQ'}) SET c.value = 5;
MATCH (c:Counter {name:'ROOMTYPE_SEQ'})  SET c.value = 2;
MATCH (c:Counter {name:'ROOM_SEQ'})      SET c.value = 20;
MATCH (c:Counter {name:'SERVICE_SEQ'})   SET c.value = 15;
MATCH (c:Counter {name:'ORDER_SEQ'})     SET c.value = 0;
MATCH (c:Counter {name:'ODR_SEQ'})       SET c.value = 0;
MATCH (c:Counter {name:'ODS_SEQ'})       SET c.value = 0;
MATCH (c:Counter {name:'UNITPRICE_SEQ'}) SET c.value = 6;
