package com.db.foodara.service.merchant;

import org.springframework.stereotype.Service;

@Service
public class MerchantOrderService {
    // 114	GET	/api/merchant/stores/:storeId/orders	Danh sách đơn hàng

    //115	GET	/api/merchant/orders/:id	Chi tiết đơn
    //116	PUT	/api/merchant/orders/:id/accept	Chấp nhận đơn
    //117	PUT	/api/merchant/orders/:id/reject	Từ chối đơn (kèm lý do)
    //118	PUT	/api/merchant/orders/:id/preparing	Chuyển sang "đang chuẩn bị"
    //119	PUT	/api/merchant/orders/:id/ready	Đánh dấu "sẵn sàng lấy hàng"
    //120	PUT	/api/merchant/orders/:id/handover	Xác nhận giao cho tài xế
    //121	WS	/ws/merchant/orders	WebSocket nhận đơn mới realtime
}
