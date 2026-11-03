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

    @Tool("广州有多少个名字的")
    public Integer guangZhouNameCount(@P("姓名") String name){
        System.out.println(name);
        return 10000;
    }

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
     * 处理退票申请
     * @param username 用户名
     * @param ticketId 票号
     * @return 退票是否成功
     */
    @Tool("根据用户名和订单号退票处理")
    public boolean processRefund(@P("用户名") String username, @P("票号") Integer ticketId) {
        return ticketService.refundTicketByUsernameAndTicketId(username, ticketId);
    }
}
