import axios from 'axios'

// 存储键常量（直接定义，避免循环依赖）
const STORAGE_KEYS = {
    user: 'user',
    artist: 'artist_user',
    admin: 'admin_user'
}

const request = axios.create({
    baseURL: 'http://localhost:8124/api',
    timeout: 10000,
    withCredentials: true
})

/**
 * 根据当前 URL 路径判断使用哪个端口的 Token
 * @returns {string|null} Token
 */
function getTokenForCurrentPortal() {
    // 规范化路径：去除尾部斜杠（但保留根路径 /）
    const rawPath = window.location.pathname
    const path = rawPath.length > 1 ? rawPath.replace(/\/$/, '') : rawPath

    let portalKey = 'user'  // 默认用户端

    // 判断当前在哪个端口
    if (path.startsWith('/admin')) {
        portalKey = 'admin'
    } else {
        // 化妆师工作台的精确路径列表
        const artistPortalPaths = ['/artist', '/artist/orders', '/artist/services', '/artist/reviews', '/artist/login']
        // 检查是否精确匹配化妆师工作台路径
        const isArtistPortal = artistPortalPaths.includes(path)

        // /artist/:id（数字ID）是用户端的化妆师详情页，使用 user token
        // 例如 /artist/3, /artist/123 等
        const isArtistDetailPage = /^\/artist\/\d+$/.test(path)

        if (isArtistPortal && !isArtistDetailPage) {
            portalKey = 'artist'
        }
        // 其他情况（/artist/3, /artists 等）都使用 user token
    }

    // 从对应存储键获取用户数据
    const storageKey = STORAGE_KEYS[portalKey]
    const userData = localStorage.getItem(storageKey)

    if (userData) {
        try {
            const user = JSON.parse(userData)
            return user.token || null
        } catch (e) {
            console.error('解析用户数据失败:', e)
            return null
        }
    }

    return null
}

// 请求拦截器
request.interceptors.request.use(
    (config) => {
        // 根据当前端口获取对应的 Token
        const token = getTokenForCurrentPortal()
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    (response) => {
        const res = response.data
        if (res.code === 0) {
            return res.data
        } else if (res.code === 40100) {
            // 未登录，根据当前端口跳转到对应登录页
            const rawPath = window.location.pathname
            const path = rawPath.length > 1 ? rawPath.replace(/\/$/, '') : rawPath

            // 公开页面不需要强制跳转登录（化妆师详情、列表等）
            // /artist/:id (数字ID) 和 /artists 是公开页面
            const isArtistDetailPage = /^\/artist\/\d+$/.test(path)
            const isPublicPage = path.startsWith('/artists') || isArtistDetailPage
            if (isPublicPage) {
                // 公开页面不跳转，只返回错误让组件静默处理
                return Promise.reject(new Error(res.message || '请先登录'))
            }

            // 精确匹配化妆师工作台路径
            const artistPortalPaths = ['/artist', '/artist/orders', '/artist/services', '/artist/reviews']
            const isArtistPortal = artistPortalPaths.includes(path) || path === '/artist/login'

            if (path.startsWith('/admin')) {
                window.location.href = '/admin/login'
            } else if (isArtistPortal) {
                window.location.href = '/artist/login'
            } else {
                window.location.href = '/login'
            }
            return Promise.reject(new Error(res.message || '请先登录'))
        } else {
            return Promise.reject(new Error(res.message || '请求失败'))
        }
    },
    (error) => {
        console.error('Request error:', error)
        return Promise.reject(error)
    }
)

export default request
