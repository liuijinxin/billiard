package com.wangpo.base.excel;

import com.wangpo.base.item.Item;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BilliardLuckyCueConfig implements IConfig{
    private int id;
    /** 白球位置 */
    private String whiteBall;
    /** 红球位置 */
    private String redBall;
    /** 奖励区圆心位置 */
    private String centerPosition;
    /** 各奖励区域半径 */
    private String radius;
    /** 从外到内各区域奖励 */
    private String award;
    /** 免费区域奖励 */
    private String freeAward;

    //vip奖励
    private List<List<Item>> vipAwardList = new ArrayList<>();

    //免费奖励
    private List<List<Item>> freeAwardList = new ArrayList<>();

    @Override
    public void explain() {
        if( award!=null && award.length()>0) {
             splitItemList(award,vipAwardList);
        }
        if( freeAward!=null && freeAward.length()>0) {
            splitItemList(freeAward,freeAwardList);
        }
    }

    private void splitItemList(String award,List<List<Item>> list) {
        String[] s = award.split("\\|");
        if(s.length > 0) {
            for(int i=0;i<s.length;i++) {
                List<Item> itemList = new ArrayList<>();
                String[] ss = s[i].split(";");
                if(ss.length > 0) {
                    for(int j=0;j<ss.length;j++) {
                        String[] sss = ss[j].split(",");
                        if( sss.length==2) {
                            Item item = new Item();
                            item.setId(Integer.parseInt(sss[0]));
                            item.setNum(Integer.parseInt(sss[1]));
                            itemList.add(item);
                        }
                    }
                }
                list.add(itemList);
            }
        }
    }
}
