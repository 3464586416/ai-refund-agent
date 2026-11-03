package com.gec.service;

import com.gec.entity.Tickets;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolsService {

    @Autowired
    TicketServiceImpl ticketService;

    @Tool("城市的天气如何")
    public String guangZhouWeather(@P("广州") String city){
        System.out.println(city+"去调用今天天气的api");
        return "晴天";
    }

    /**
     * 根据用户名查询票务信息
     * @param username 用户名
     * @return 票务信息列表
     */
    @Tool("根据提供的用户名查询该用户的所有票务信息")
    public List<Tickets> queryTicketsByUsername(@P("用户名") String username) {
        List<Tickets> ticketsList = ticketService.getBookedTicketsByUsername(username);
        for (Tickets tickets : ticketsList) {
            System.out.println(tickets);
        }
        return ticketsList;
    }

    /**
     * 处理退票申请（需手机号身份核验）
     * @param username 用户名
     * @param ticketId 票号
     * @param phone 注册手机号
     * @return 退票是否成功
     */
    @Tool("根据用户名、订单号和注册手机号办理退票；手机号与账号预留号码不一致时核验不通过，退票失败")
    public boolean processRefund(@P("用户名") String username,
                                 @P("票号") Integer ticketId,
                                 @P("注册手机号") String phone) {
        return ticketService.refundTicketByUsernameAndTicketId(username, ticketId, phone);
    }
}
