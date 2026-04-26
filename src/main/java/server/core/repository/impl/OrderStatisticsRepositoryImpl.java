package server.core.repository.impl;

import common.dto.DailyDetailDTO;
import common.dto.OrderStatisticsDTO;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import server.core.repository.OrderStatisticsRepository;
import server.infrastructure.db.Neo4jConnManager;
import server.infrastructure.mapper.GenericDataMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderStatisticsRepositoryImpl implements OrderStatisticsRepository {

    private final Neo4jConnManager connManager;
    private final GenericDataMapper mapper;

    public OrderStatisticsRepositoryImpl(Neo4jConnManager connManager, GenericDataMapper mapper) {
        this.connManager = connManager;
        this.mapper = mapper;
    }

    @Override
    public OrderStatisticsDTO getDailyOrderStatistics(LocalDateTime dateTime) {
        String query = """
                MATCH (o:Order)
                WHERE o.orderStatus = 'Thanh toán'
                  AND date(o.orderDate) = date($dateTime)
                RETURN count(o) AS soLuongHoaDon,
                       coalesce(sum(o.total), 0) AS tongThuNhap
                """;

        Map<String, Object> params = Map.of("dateTime", dateTime);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                List<OrderStatisticsDTO> list = result.list()
                        .stream()
                        .map(record -> {
                            Map<String, Object> data = new HashMap<>();
                            data.put("soLuongHoaDon", record.get("soLuongHoaDon").asInt());
                            data.put("tongThuNhap", record.get("tongThuNhap").asDouble());
                            return mapper.toObject(data, OrderStatisticsDTO.class);
                        })
                        .toList();

                return list.isEmpty() ? new OrderStatisticsDTO(0, 0) : list.get(0);
            });
        }
    }

    @Override
    public DailyDetailDTO getDetailByRange(LocalDateTime start, LocalDateTime end) {
        String query = """
            MATCH (o:Order)
            WHERE o.orderStatus = 'Thanh toán'
              AND o.orderDate >= $start
              AND o.orderDate <= $end
            WITH collect(o) AS orders

            CALL {
                WITH orders
                UNWIND orders AS o
                RETURN count(o) AS soLuongHoaDon
            }

            CALL {
                WITH orders
                UNWIND orders AS o
                OPTIONAL MATCH (o)-[:HAS_ROOM_DETAIL]->(odr:OrderDetailRoom)
                RETURN count(odr) AS totalBookings,
                       coalesce(sum(odr.roomFee), 0) AS roomRevenue
            }

            CALL {
                WITH orders
                UNWIND orders AS o
                OPTIONAL MATCH (o)-[:HAS_SERVICE_DETAIL]->(ods:OrderDetailService)
                RETURN coalesce(sum(ods.quantity), 0) AS totalServiceQty,
                       coalesce(sum(ods.serviceFee), 0) AS serviceRevenue
            }

            RETURN soLuongHoaDon,
                   totalBookings,
                   totalServiceQty,
                   roomRevenue,
                   serviceRevenue,
                   (roomRevenue + serviceRevenue) AS totalRevenue
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("start", start);
        params.put("end", end);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);

                List<DailyDetailDTO> list = result.list()
                        .stream()
                        .map(record -> {
                            Map<String, Object> data = new HashMap<>();
                            data.put("soLuongHoaDon", record.get("soLuongHoaDon").asInt());
                            data.put("totalBookings", record.get("totalBookings").asInt());
                            data.put("totalServiceQty", record.get("totalServiceQty").asInt());
                            data.put("roomRevenue", record.get("roomRevenue").asDouble());
                            data.put("serviceRevenue", record.get("serviceRevenue").asDouble());
                            data.put("totalRevenue", record.get("totalRevenue").asDouble());
                            return mapper.toObject(data, DailyDetailDTO.class);
                        })
                        .toList();

                return list.isEmpty()
                        ? new DailyDetailDTO(0, 0, 0, 0, 0, 0)
                        : list.get(0);
            });
        }
    }

    @Override
    public double getRevenueByRange(LocalDateTime start, LocalDateTime end) {
        String query = """
            MATCH (o:Order)
            WHERE o.orderStatus = 'Thanh toán'
              AND o.orderDate >= $start
              AND o.orderDate <= $end
            WITH collect(o) AS orders

            CALL {
                WITH orders
                UNWIND orders AS o
                OPTIONAL MATCH (o)-[:HAS_ROOM_DETAIL]->(odr:OrderDetailRoom)
                RETURN coalesce(sum(odr.roomFee), 0) AS roomRevenue
            }

            CALL {
                WITH orders
                UNWIND orders AS o
                OPTIONAL MATCH (o)-[:HAS_SERVICE_DETAIL]->(ods:OrderDetailService)
                RETURN coalesce(sum(ods.serviceFee), 0) AS serviceRevenue
            }

            RETURN (roomRevenue + serviceRevenue) AS totalRevenue
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("start", start);
        params.put("end", end);

        try (Session session = connManager.openSession()) {
            return session.executeRead(tx -> {
                Result result = tx.run(query, params);
                return result.single().get("totalRevenue").asDouble();
            });
        }
    }
}