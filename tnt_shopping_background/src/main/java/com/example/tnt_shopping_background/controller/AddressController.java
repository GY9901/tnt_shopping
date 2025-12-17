package com.example.tnt_shopping_background.controller;

import com.example.tnt_shopping_background.common.Result;
import com.example.tnt_shopping_background.entity.Address;
import com.example.tnt_shopping_background.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressRepository addressRepository;

    // 获取用户地址列表
    @GetMapping("/list")
    public Result<List<Address>> getAddresses(@RequestParam String username) {
        List<Address> addresses = addressRepository.findByUsername(username);
        return Result.success(addresses);
    }

    // 添加地址
    @PostMapping("/add")
    @Transactional
    public Result<?> addAddress(@RequestBody Address address) {
        // 如果设置为默认地址，将其他地址设置为非默认
        if (address.getIsDefault() != null && address.getIsDefault()) {
            List<Address> existingAddresses = addressRepository.findByUsername(address.getUsername());
            for (Address existing : existingAddresses) {
                existing.setIsDefault(false);
                addressRepository.save(existing);
            }
        }
        addressRepository.save(address);
        return Result.success("添加地址成功");
    }

    // 更新地址
    @PutMapping("/update")
    @Transactional
    public Result<?> updateAddress(@RequestBody Address address) {
        // 如果设置为默认地址，将其他地址设置为非默认
        if (address.getIsDefault() != null && address.getIsDefault()) {
            List<Address> existingAddresses = addressRepository.findByUsername(address.getUsername());
            for (Address existing : existingAddresses) {
                if (!existing.getId().equals(address.getId())) {
                    existing.setIsDefault(false);
                    addressRepository.save(existing);
                }
            }
        }
        addressRepository.save(address);
        return Result.success("更新地址成功");
    }

    // 删除地址
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteAddress(@PathVariable Integer id) {
        addressRepository.deleteById(id);
        return Result.success("删除地址成功");
    }

    // 设置默认地址
    @PostMapping("/setDefault")
    @Transactional
    public Result<?> setDefaultAddress(@RequestBody Map<String, Object> payload) {
        Integer id = Integer.parseInt(payload.get("id").toString());
        String username = (String) payload.get("username");
        
        // 将所有地址设置为非默认
        List<Address> addresses = addressRepository.findByUsername(username);
        for (Address address : addresses) {
            address.setIsDefault(false);
            addressRepository.save(address);
        }
        
        // 将指定地址设置为默认
        Address targetAddress = addressRepository.findById(id).orElseThrow(() -> new RuntimeException("地址不存在"));
        targetAddress.setIsDefault(true);
        addressRepository.save(targetAddress);
        
        return Result.success("设置默认地址成功");
    }
}
