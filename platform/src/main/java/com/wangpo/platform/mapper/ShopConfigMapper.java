package com.wangpo.platform.mapper;

import com.wangpo.base.excel.ShopConfig;
import com.wangpo.platform.bean.Player;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@Mapper
public interface ShopConfigMapper {

    @Select("select goods_id,status,goods_name,show_name,pay_type,price,goods_type,count,add_count,even_limit,day_limit,promotion_type,promotion_value,effect_times," +
            "cycle_times,room_limit,channal_limit,itemId,bonus_item from shop_config")
    @Results(value = {
            @Result(property = "goodsId", column = "goods_id"),
            @Result(property = "status", column = "status"),
            @Result(property = "goodsName", column = "goods_name"),
            @Result(property = "showName", column = "show_name"),
            @Result(property = "payType", column = "pay_type"),
            @Result(property = "price", column = "price"),
            @Result(property = "goodsType", column = "goods_type"),
            @Result(property = "count", column = "count"),
            @Result(property = "addCount", column = "add_count"),
            @Result(property = "evenLimit", column = "even_limit"),
            @Result(property = "dayLimit", column = "day_limit"),
            @Result(property = "promotionType", column = "promotion_type"),
            @Result(property = "promotionValue", column = "promotion_value"),
            @Result(property = "effectTimes", column = "effect_times"),
            @Result(property = "cycleTimes", column = "cycle_times"),
            @Result(property = "roomLimit", column = "room_limit"),
            @Result(property = "channalLimit", column = "channal_limit"),
            @Result(property = "itemId", column = "itemId"),
            @Result(property = "bonusItem", column = "bonus_item",jdbcType = JdbcType.OTHER, typeHandler = com.wangpo.platform.mapper.MySqlJsonHandler.class)
    })
    List<ShopConfig> getShopConfig();
    
    @Insert("insert into player (goods_id,status,goods_name,show_name,pay_type,price,goods_type,count,add_count,even_limit,day_limit,promotion_type,promotion_value,effect_times,cycle_times,room_limit,channal_limit,itemId,bonus_item from shop_config) " +
            "value(#{goodsId},#{status},#{goodsName},#{showName},#{payType},#{price},#{goodsType},#{count},#{addCount},#{evenLimit},#{dayLimit},#{promotionType},#{promotionValue},#{effectTimes},#{cycleTimes},#{roomLimit},#{channalLimit},#{itemId},#{bonusItem})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertShopConfig(ShopConfig shopConfig);
    
    @Update("update shop_config set status= #{status} where goods_id = #{goodsId}")
    int updateShopConfig(ShopConfig shopConfig);
    
    @Delete("delete from shop_config where goods_id = #{id}")
    int deleteShopConfig(long id);


}
