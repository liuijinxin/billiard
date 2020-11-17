package com.wangpo.platform.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.wangpo.platform.dao.BuyOrder;


@Mapper
public interface BuyOrderMapper {
	
	
	@Select("select id,pay_ment_Sn,add_time,user_id,goods_id,goods_tip_name,pay_ment_code,order_status,order_source,order_amount,pay_config_id"+
            " from shop_order where pay_ment_Sn = #{paymentSn}")
    @Results(value = {
    		@Result(property = "id", column = "id"),
            @Result(property = "paymentSn", column = "pay_ment_Sn"),
            @Result(property = "addTime", column = "add_time"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "goodsId", column = "goods_id"),
            @Result(property = "goodsTipName", column = "goods_tip_name"),
            @Result(property = "paymentCode", column = "pay_ment_code"),
            @Result(property = "orderStatus", column = "order_status"),
            @Result(property = "orderSource", column = "order_source"),
            @Result(property = "orderAmount", column = "order_amount"),
            @Result(property = "payConfigId", column = "pay_config_id")
    })
	BuyOrder selectBuyOrder(String paymentSn);
	

    @Insert("insert into shop_order (pay_ment_Sn,add_time,user_id,goods_id,goods_tip_name,pay_ment_code,order_status,order_source,order_amount,pay_config_id) " +
            "value(#{paymentSn},#{addTime},#{userId},#{goodsId},#{goodsTipName},#{paymentCode},#{orderStatus},#{orderSource},#{orderAmount},#{payConfigId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertBuyOrder(BuyOrder order);
    
    @Update("update shop_order set order_status = #{orderStatus} where pay_ment_Sn = #{paymentSn}")
    int updateBuyOrder(BuyOrder order);

}
