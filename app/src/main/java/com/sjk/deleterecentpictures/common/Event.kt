package com.sjk.deleterecentpictures.common


class Event {
    private val events: MutableMap<String, ( args: Array<Any?>) -> Unit> = mutableMapOf()

    fun addEventListener(event: String, callback: (args: Array<Any?>) -> Unit) {
        this.events[event]  = callback
    }
    
    fun removeEventListener(event: String) {
        this.events.remove(event)
    }
    
    fun fireEvent(event: String, vararg args: Any?) {
        this.events[event]?.invoke(args.toList().toTypedArray())
    }
    
}
