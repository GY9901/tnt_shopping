package com.example.tnt_shopping_background.controller;

import com.example.tnt_shopping_background.common.Result;
import com.example.tnt_shopping_background.entity.Order;
import com.example.tnt_shopping_background.entity.OrderItem;
import com.example.tnt_shopping_background.repository.CartItemRepository;
import com.example.tnt_shopping_background.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    // 获取用户的订单列表 (分页版)
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<Order> orderPage = orderRepository.findByUsernameOrderByCreateTimeDesc(username, pageable);

        Map<String, Object> data = new HashMap<>();
        data.put("list", orderPage.getContent());
        data.put("total", orderPage.getTotalElements());

        return Result.success(data);
    }

    // [新增] 管理员获取所有订单 (不分页或简单分页，这里演示获取所有)
    @GetMapping("/all")
    public Result<List<Order>> all() {
        return Result.success(orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createTime")));
    }

    // 创建订单 (下单)
    @PostMapping("/create")
    @Transactional // 开启事务
    public Result<?> create(@RequestBody Map<String, Object> payload) {
        try {
            String username = (String) payload.get("username");
            BigDecimal totalAmount = new BigDecimal(payload.get("totalAmount").toString());
            String paymentMethod = (String) payload.getOrDefault("paymentMethod", "ALIPAY");

            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) payload.get("items");
            List<Integer> cartIds = (List<Integer>) payload.get("cartIds");

            Order order = new Order();
            order.setUsername(username);
            order.setTotalAmount(totalAmount);
            order.setStatus("COMPLETED");
            order.setPaymentMethod(paymentMethod);

            String orderNo = "TNT" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + (int)(Math.random() * 1000);
            order.setOrderNo(orderNo);

            List<OrderItem> orderItems = new ArrayList<>();
            for (Map<String, Object> itemMap : itemsData) {
                OrderItem oi = new OrderItem();
                oi.setProductName((String) itemMap.get("productName"));
                oi.setProductImg((String) itemMap.get("productImg"));
                oi.setPrice(new BigDecimal(itemMap.get("price").toString()));
                oi.setQuantity((Integer) itemMap.get("quantity"));

                oi.setOrder(order);
                orderItems.add(oi);
            }
            order.setItems(orderItems);

            orderRepository.save(order);

            if (cartIds != null && !cartIds.isEmpty()) {
                cartItemRepository.deleteAllById(cartIds);
            }

            return Result.success(order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "下单失败: " + e.getMessage());
        }
    }
}