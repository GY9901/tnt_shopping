package com.example.tnt_shopping_background.controller;

import com.example.tnt_shopping_background.common.Result;
import com.example.tnt_shopping_background.entity.Order;
import com.example.tnt_shopping_background.entity.OrderItem;
import com.example.tnt_shopping_background.repository.CartItemRepository;
import com.example.tnt_shopping_background.repository.OrderRepository;
import com.example.tnt_shopping_background.repository.ProductRepository;
import com.example.tnt_shopping_background.entity.Product;
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
    
    @Autowired
    private ProductRepository productRepository;

    // 获取用户的订单列表 (分页版)
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<Order> orderPage;

        if (username != null && !username.isEmpty()) {
            orderPage = orderRepository.findByUsernameOrderByCreateTimeDesc(username, pageable);
        } else {
            orderPage = orderRepository.findAll(pageable);
        }


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
            order.setStatus("PAYED");
            order.setPaymentMethod(paymentMethod);
            // 添加地址和电话信息
            if (payload.containsKey("address")) {
                order.setAddress((String) payload.get("address"));
            }
            if (payload.containsKey("phone")) {
                order.setPhone((String) payload.get("phone"));
            }

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
                
                // 扣减商品库存
                Integer productId = (Integer) itemMap.get("productId");
                Integer quantity = (Integer) itemMap.get("quantity");
                Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("商品不存在: " + productId));
                
                // 检查库存是否充足
                if (product.getStock() < quantity) {
                    throw new RuntimeException("商品库存不足: " + product.getName());
                }
                
                // 更新库存
                product.setStock(product.getStock() - quantity);
                productRepository.save(product);
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

    // 更新订单状态
    @PutMapping("/admin/updateStatus")
    public Result<?> updateStatus(@RequestBody Map<String, Object> payload) {
        try {
            Integer id = Integer.parseInt(payload.get("id").toString());
            String status = (String) payload.get("status");

            // 查找订单
            Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("订单不存在"));

            // 更新状态
            order.setStatus(status);
            orderRepository.save(order);

            return Result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "更新订单状态失败: " + e.getMessage());
        }
    }
    
    // 删除订单
    @DeleteMapping("/admin/delete/{id}")
    @Transactional // 开启事务
    public Result<?> deleteOrder(@PathVariable Integer id) {
        try {
            // 查找订单
            Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("订单不存在"));
            
            // 检查订单状态，只有未发货的订单才能回滚库存
            // 假设 PENDING 和 PAYED 状态为未发货，其他状态为已处理
            if ("PAYED".equals(order.getStatus()) || "PENDING".equals(order.getStatus())) {
                // 遍历订单商品，回滚库存
                for (OrderItem item : order.getItems()) {
                    // 根据商品名称查询商品
                    Product product = productRepository.findByName(item.getProductName());
                    if (product != null) {
                        // 回滚库存
                        product.setStock(product.getStock() + item.getQuantity());
                        productRepository.save(product);
                    }
                }
            }
            
            // 删除订单
            orderRepository.delete(order);
            
            return Result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "删除订单失败: " + e.getMessage());
        }
    }
    
    // 取消订单
    @PutMapping("/cancel/{id}")
    @Transactional // 开启事务
    public Result<?> cancelOrder(@PathVariable Integer id) {
        try {
            // 查找订单
            Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("订单不存在"));
            
            // 检查订单状态，只有未发货的订单才能取消
            if (!"PAYED".equals(order.getStatus()) && !"PENDING".equals(order.getStatus())) {
                return Result.error("400", "当前订单状态不允许取消");
            }
            
            // 更新订单状态为 CANCELLED
            order.setStatus("CANCELLED");
            
            // 遍历订单商品，回滚库存
            for (OrderItem item : order.getItems()) {
                // 根据商品名称查询商品
                Product product = productRepository.findByName(item.getProductName());
                if (product != null) {
                    // 回滚库存
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepository.save(product);
                }
            }
            
            // 保存更新后的订单
            orderRepository.save(order);
            
            return Result.success(null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("500", "取消订单失败: " + e.getMessage());
        }
    }
}