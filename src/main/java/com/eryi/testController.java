package com.eryi;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: ZouJiaNan
 * @date: 2022/2/7 14:19
 */
@RestController
public class testController {
    @RequestMapping("test")
    public void test(){
        try {
            ClashPreventUtils.check();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
