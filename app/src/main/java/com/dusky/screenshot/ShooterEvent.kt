package com.dusky.screenshot


open class ShooterEvent {
    var event_todo: Int = 0
    var y:Int=0
    var yv:Int=0
    companion object {
        var EventTakePhoto = 0x1043
        var EventServiceStartFind = 0x1044
        var EventPhotoNext = 0x1045
    }
}
