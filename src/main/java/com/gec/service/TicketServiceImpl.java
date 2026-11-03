package com.gec.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gec.entity.RefundRecords;
import com.gec.entity.Tickets;
import com.gec.entity.Users;
import com.gec.mapper.RefundRecordsMapper;
import com.gec.mapper.TicketsMapper;
import com.gec.mapper.UsersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TicketServiceImpl {

    @Autowired
    private TicketsMapper ticketMapper;

    @Autowired
    private RefundRecordsMapper refundRecordMapper;

    @Autowired
    private UsersMapper userMapper;

    /**
     * 根据用户名查询用户的订票信息
     * @param username 用户名
     * @return 订票信息列表
     */
    public List<Tickets> getBookedTicketsByUsername(String username) {
        Optional<Integer> userId = getUserIdByUsername(username);
        if (userId.isPresent()) {
            QueryWrapper<Tickets> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId.get())
                    .eq("is_refunded", false);
            return ticketMapper.selectList(wrapper);
        }
        return null;
    }

    /**
     * 根据用户名、票号和注册手机号退票（手机号核验身份）
     * @param username 用户名
     * @param ticketId 车票 ID
     * @param phone    用户声称的注册手机号，需与账号预留号码一致
     * @return 退票是否成功
     */
    public boolean refundTicketByUsernameAndTicketId(String username, Integer ticketId, String phone) {
        Optional<Integer> userId = getUserIdByUsername(username);
        if (userId.isPresent()) {
            // 身份核验：手机号必须与账号预留号码一致，防止仅凭姓名+票号恶意退票
            Users user = userMapper.selectById(userId.get());
            if (user == null || user.getPhone() == null
                    || phone == null || !phone.trim().equals(user.getPhone().trim())) {
                System.out.println("[退票被拒] 手机号核验未通过: username=" + username);
                return false;
            }
            Tickets ticket = ticketMapper.selectById(ticketId);
            if (ticket != null && ticket.getUserId().equals(userId.get()) && !ticket.getIsRefunded()) {
                // 计算退票金额
                BigDecimal refundAmount = ticket.getPrice();

                // 创建退票记录
                RefundRecords refundRecord = new RefundRecords();
                refundRecord.setTicketId(ticketId);
                refundRecord.setRefundTime(LocalDateTime.now());
                refundRecord.setRefundAmount(refundAmount);
                refundRecordMapper.insert(refundRecord);

                // 更新车票状态为已退票
                ticket.setIsRefunded(true);
                ticketMapper.updateById(ticket);

                return true;
            }
        }
        return false;
    }

    /**
     * 根据姓名查询用户ID
     * @param username 姓名
     * @return 用户ID
     */
    private Optional<Integer> getUserIdByUsername(String username) {
        QueryWrapper<Users> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        Users user = userMapper.selectOne(wrapper);
        return user != null ? Optional.of(user.getUserId()) : Optional.empty();
    }
}
