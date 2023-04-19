package com.tempura.threadtx.test.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wzs
 * @since 2023/4/18 19:24
 */
@Data
@NoArgsConstructor
public class CarInfo {


    @TableId(type = IdType.AUTO)
    private Integer id;

    private String carName;

    public CarInfo(String carName){
        this.carName = carName;
    }
}
