import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// 三端存储键常量
export const PORTAL_KEYS = {
    user: 'user',           // 用户端
    artist: 'artist_user',   // 化妆师端
    admin: 'admin_user'      // 管理端
}

export const useUserStore = defineStore('user', () => {
    // 当前端口的用户（保持向后兼容）
    const user = ref(null)
    const token = ref(null)

    // 计算属性（基于当前 user）
    const isLoggedIn = computed(() => !!user.value)
    const isArtist = computed(() => user.value?.role === 1)
    const isAdmin = computed(() => user.value?.role === 2)

    // ========== 原有方法（保持向后兼容）==========

    function setUser(userData) {
        user.value = userData
        localStorage.setItem('user', JSON.stringify(userData))
    }

    function setToken(tokenValue) {
        token.value = tokenValue
        localStorage.setItem('token', tokenValue)
    }

    function logout() {
        user.value = null
        token.value = null
        localStorage.removeItem('user')
        localStorage.removeItem('token')
    }

    function init() {
        const storedUser = localStorage.getItem('user')
        const storedToken = localStorage.getItem('token')
        if (storedUser) {
            user.value = JSON.parse(storedUser)
        }
        if (storedToken) {
            token.value = storedToken
        }
    }

    // ========== 多端口方法 ==========

    // 根据端口设置用户
    function setUserByPortal(portal, userData) {
        const key = PORTAL_KEYS[portal] || 'user'
        localStorage.setItem(key, JSON.stringify(userData))
        // 如果是用户端，同时更新响应式状态
        if (portal === 'user') {
            user.value = userData
        }
    }

    // 根据端口获取用户
    function getUserByPortal(portal) {
        const key = PORTAL_KEYS[portal] || 'user'
        const stored = localStorage.getItem(key)
        return stored ? JSON.parse(stored) : null
    }

    // 根据端口登出
    function logoutByPortal(portal) {
        const key = PORTAL_KEYS[portal] || 'user'
        localStorage.removeItem(key)
        localStorage.removeItem(key + '_token')
        // 如果是用户端，同时清除响应式状态
        if (portal === 'user') {
            user.value = null
            token.value = null
        }
    }

    // 检查指定端口是否已登录
    function isLoggedInByPortal(portal) {
        return !!getUserByPortal(portal)
    }

    return {
        user,
        token,
        isLoggedIn,
        isArtist,
        isAdmin,
        setUser,
        setToken,
        logout,
        init,
        // 多端口方法
        setUserByPortal,
        getUserByPortal,
        logoutByPortal,
        isLoggedInByPortal
    }
})
