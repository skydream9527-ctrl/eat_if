package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import com.eatif.app.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SetupViewModel : ViewModel() {

    private val _shops = MutableStateFlow<List<String>>(emptyList())
    val shops: StateFlow<List<String>> = _shops.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** 内置美食品牌/店铺词库（30+）*/
    private val foodBrandPool = listOf(
        "海底捞", "喜家德水饺", "西贝莜面村", "太二酸菜鱼", "外婆家",
        "麦当劳", "肯德基", "必胜客", "汉堡王", "星巴克",
        "喜茶", "奈雪的茶", "一点点", "蜜雪冰城", "茉莉奶白",
        "和府捞面", "遇见小面", "陈香贵兰州牛肉面", "马记永", "张拉拉",
        "老乡鸡", "真功夫", "吉野家", "丸龟制面", "萨莉亚",
        "绿茶餐厅", "云海肴", "大渝火锅", "呷哺呷哺", "巴奴毛肚火锅",
        "探鱼", "九毛九", "费大厨辣椒炒肉", "文和友", "湊湊火锅"
    )

    fun onInputChange(text: String) {
        _inputText.value = text
        _errorMessage.value = null
    }

    fun addShop() {
        val name = _inputText.value.trim()
        if (name.isBlank()) {
            _errorMessage.value = "店铺名不能为空"
            return
        }
        if (name.length > 20) {
            _errorMessage.value = "店铺名太长了（最多20字）"
            return
        }
        if (_shops.value.contains(name)) {
            _errorMessage.value = "已经添加过这家店了"
            return
        }
        if (_shops.value.size >= 10) {
            _errorMessage.value = "最多只能添加10个选项"
            return
        }
        _shops.value = _shops.value + name
        _inputText.value = ""
        _errorMessage.value = null
    }

    fun removeShop(shop: String) {
        _shops.value = _shops.value.filter { it != shop }
    }

    fun generateRandom() {
        val current = _shops.value.toMutableList()
        val available = foodBrandPool.filter { it !in current }.shuffled()
        val toAdd = available.take((10 - current.size).coerceAtLeast(0))
        // 如果已有内容，合并；如果为空，直接取前10
        val result = if (current.isEmpty()) {
            foodBrandPool.shuffled().take(10)
        } else {
            (current + toAdd).take(10)
        }
        _shops.value = result
        _errorMessage.value = null
    }

    fun canConfirm(): Boolean = _shops.value.size >= 3

    fun confirm() {
        SessionManager.shopOptions = _shops.value
    }
}
