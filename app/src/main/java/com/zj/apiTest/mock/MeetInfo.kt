package com.zj.apiTest.mock

class MeetInfo {

    enum class MeetStatus(var apiStatus: Int) {
        PREPARING(0), PROGRESSING(1), ENDED(2)
    }

    var id: Int = 0

    //会议状态 0-未开始,1-进行中,2-已结束
    var status: Int = 0

    //会议 Code
    var meetingCode: String? = ""

    //创建者id
    var ownerId: Int = 0

    //创建者名
    var ownerName: String? = ""

    //创建者形象
    var ownerAvatar: String? = ""

    //主题
    var name: String? = "asdasdasfasdasdasdfasd"

    //开始时间
    var startTime: Long? = System.currentTimeMillis() + 100111

    //结束时间
    var endTime: Long? = System.currentTimeMillis() + 100123141

    //有无秘钥
    var needSecret: Boolean = false

    //秘钥
    var secret: String? = ""

    //提醒时间
    var remindTime: Long = 0
}