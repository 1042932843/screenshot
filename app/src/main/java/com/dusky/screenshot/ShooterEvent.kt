package com.dusky.screenshot


open class ShooterEvent {
    var eventTodo: Int = 0
    var eventMsg: String = ""
    var y:Int=0
    var yv:Int=0
    companion object {
        var EventTakePhoto = 0x1043
        var EventServiceStartStep0 = 0x1044
        var EventServiceStartStep1 = 0x1045
        var EventErrorNext = 0x1046
        var EventNext = 0x1047
    }
}
