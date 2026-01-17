import request from './request'

// 用户认证
export const userApi = {
    register: (data) => request.post('/user/register', data),
    login: (data) => request.post('/user/login', data),
    logout: () => request.post('/user/logout'),
    getCurrent: () => request.get('/user/current'),
    updateProfile: (data) => request.put('/user/update', data),
    // 化妆师入驻申请
    artistApply: (data) => request.post('/user/artist/apply', data),
    getApplicationStatus: () => request.get('/user/artist/apply/status')
}

// 化妆师
export const artistApi = {
    getList: (params) => request.get('/artist/list', { params }),
    getDetail: (id) => request.get(`/artist/${id}`),
    getRecommend: (limit = 6) => request.get('/artist/recommend', { params: { limit } }),
    search: (keyword, current = 1, pageSize = 10) =>
        request.get('/artist/search', { params: { keyword, current, pageSize } }),
    getServices: (id) => request.get(`/artist/${id}/services`)
}

// 预约
export const appointmentApi = {
    create: (data) => request.post('/appointment/create', data),
    cancel: (id, data) => request.post(`/appointment/cancel/${id}`, data),
    reschedule: (id, data) => request.put(`/appointment/reschedule/${id}`, data),
    getMyList: (params) => request.get('/appointment/my/list', { params }),
    getDetail: (id) => request.get(`/appointment/${id}`)
}

// 支付
export const paymentApi = {
    getInfo: (appointmentId) => request.get(`/payment/info/${appointmentId}`),
    pay: (data) => request.post('/payment/pay', data),
    getStatus: (paymentNo) => request.get(`/payment/status/${paymentNo}`),
    refund: (data) => request.post('/payment/refund', data)
}

// 收藏
export const favoriteApi = {
    addArtist: (artistId) => request.post(`/favorite/artist/${artistId}`),
    removeArtist: (artistId) => request.delete(`/favorite/artist/${artistId}`),
    addService: (serviceId) => request.post(`/favorite/service/${serviceId}`),
    removeService: (serviceId) => request.delete(`/favorite/service/${serviceId}`),
    getMyList: (params) => request.get('/favorite/my/list', { params }),
    check: (targetType, targetId) =>
        request.get('/favorite/check', { params: { targetType, targetId } })
}

// 评价
export const reviewApi = {
    create: (data) => request.post('/review/create', data),
    getArtistReviews: (artistId, current = 1, pageSize = 10) =>
        request.get(`/review/artist/${artistId}`, { params: { current, pageSize } })
}

// 化妆师服务套餐管理
export const artistServiceApi = {
    create: (data) => request.post('/artist/service/create', data),
    update: (id, data) => request.put(`/artist/service/update/${id}`, data),
    updateStatus: (id, status) => request.put(`/artist/service/status/${id}`, { status }),
    delete: (id) => request.delete(`/artist/service/delete/${id}`),
    getMyList: (params) => request.get('/artist/service/my/list', { params })
}

// 文件上传
export const uploadApi = {
    uploadAvatar: (file) => {
        const formData = new FormData()
        formData.append('file', file)
        return request.post('/upload/avatar', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        })
    },
    uploadImage: (file, directory = 'common') => {
        const formData = new FormData()
        formData.append('file', file)
        formData.append('directory', directory)
        return request.post('/upload/image', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        })
    }
}

// 管理员API
export const adminApi = {
    // 统计
    getStats: () => request.get('/admin/stats'),

    // 用户管理
    getUserList: (params) => request.get('/admin/user/list', { params }),
    createUser: (data) => request.post('/admin/user/create', data),
    updateUser: (id, data) => request.put(`/admin/user/update/${id}`, data),
    updateUserStatus: (id, status) => request.put(`/admin/user/status/${id}`, { status }),
    deleteUser: (id) => request.delete(`/admin/user/delete/${id}`),

    // 化妆师管理
    getArtistList: (params) => request.get('/admin/artist/list', { params }),
    getArtistDetail: (id) => request.get(`/admin/artist/${id}`),
    updateArtist: (id, data) => request.put(`/admin/artist/update/${id}`, data),
    updateArtistStatus: (id, status) => request.put(`/admin/artist/status/${id}`, { status }),
    setArtistRecommend: (id, isRecommend) => request.put(`/admin/artist/recommend/${id}`, { status: isRecommend }),
    deleteArtist: (id) => request.delete(`/admin/artist/delete/${id}`),

    // 入驻审核
    getApplicationList: (params) => request.get('/admin/artist/application/list', { params }),
    getApplicationDetail: (id) => request.get(`/admin/artist/application/${id}`),
    approveApplication: (id) => request.post(`/admin/artist/application/approve/${id}`),
    rejectApplication: (id, rejectReason) => request.post(`/admin/artist/application/reject/${id}`, { rejectReason }),

    // 预约管理
    getAppointmentList: (params) => request.get('/admin/appointment/list', { params }),
    getAppointmentDetail: (id) => request.get(`/admin/appointment/${id}`),

    // 评价管理
    getReviewList: (params) => request.get('/admin/review/list', { params }),
    deleteReview: (id) => request.delete(`/admin/review/delete/${id}`),

    // 服务套餐管理
    getServiceList: (params) => request.get('/admin/service/list', { params }),
    getServiceDetail: (id) => request.get(`/admin/service/${id}`),
    createService: (data) => request.post('/admin/service/create', data),
    updateService: (id, data) => request.put(`/admin/service/update/${id}`, data),
    updateServiceStatus: (id, status) => request.put(`/admin/service/status/${id}`, { status }),
    deleteService: (id) => request.delete(`/admin/service/delete/${id}`)
}

// 化妆师端口API
export const artistPortalApi = {
    // 个人信息
    getProfile: () => request.get('/artist-portal/profile'),

    // 预约管理
    getAppointments: (params) => request.get('/artist-portal/appointments', { params }),
    confirmAppointment: (id) => request.post(`/artist-portal/appointment/confirm/${id}`),
    rejectAppointment: (id, reason) => request.post(`/artist-portal/appointment/reject/${id}`, { reason }),

    // 工作状态
    getWorkStatus: () => request.get('/artist-portal/status'),
    setWorkStatus: (status) => request.put('/artist-portal/status', { status }),

    // 服务套餐
    getServices: () => request.get('/artist-portal/services'),
    createService: (data) => request.post('/artist-portal/service', data),
    updateService: (id, data) => request.put(`/artist-portal/service/${id}`, data),
    updateServiceStatus: (id, status) => request.put(`/artist-portal/service/status/${id}`, { status }),
    deleteService: (id) => request.delete(`/artist-portal/service/${id}`),

    // 评价管理
    getReviews: (params) => request.get('/artist-portal/reviews', { params }),
    replyReview: (id, reply) => request.post(`/artist-portal/review/reply/${id}`, { reply })
}
