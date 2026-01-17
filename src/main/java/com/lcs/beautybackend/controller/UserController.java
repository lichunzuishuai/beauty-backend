package com.lcs.beautybackend.controller;

import com.lcs.beautybackend.common.BaseResponse;
import com.lcs.beautybackend.common.ResultUtils;
import com.lcs.beautybackend.exception.BusinessException;
import com.lcs.beautybackend.exception.ErrorCode;
import com.lcs.beautybackend.exception.ThrowUtils;
import com.lcs.beautybackend.model.dto.artistapplication.ArtistApplyRequest;
import com.lcs.beautybackend.model.dto.user.UserLoginRequest;
import com.lcs.beautybackend.model.dto.user.UserRegisterRequest;
import com.lcs.beautybackend.model.dto.user.UserUpdateRequest;
import com.lcs.beautybackend.model.entity.User;
import com.lcs.beautybackend.model.vo.ArtistApplicationVO;
import com.lcs.beautybackend.model.vo.LoginUserVO;
import com.lcs.beautybackend.model.vo.UserVO;
import com.lcs.beautybackend.service.ArtistapplicationService;
import com.lcs.beautybackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户接口
 */
@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "用户模块", description = "用户认证相关接口")
public class UserController {

    @Resource
    private UserService userService;

    // region 登录注册

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册账号")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String username = userRegisterRequest.getUsername();
        String password = userRegisterRequest.getPassword();
        String confirmPassword = userRegisterRequest.getConfirmPassword();
        String phone = userRegisterRequest.getPhone();
        long result = userService.userRegister(username, password, confirmPassword, phone);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录获取会话")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String username = userLoginRequest.getUsername();
        String password = userLoginRequest.getPassword();
        LoginUserVO loginUserVO = userService.userLogin(username, password, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出登录")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    // endregion

    // region 用户信息

    /**
     * 获取当前登录用户
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前登录用户", description = "获取当前登录用户信息（已脱敏）")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    /**
     * 根据ID获取用户信息（脱敏）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息", description = "根据ID获取用户公开信息（已脱敏）")
    public BaseResponse<UserVO> getUserById(@PathVariable Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/update")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户信息")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);

        User updateUser = new User();
        updateUser.setId(loginUser.getId());
        updateUser.setNickname(userUpdateRequest.getNickname());
        updateUser.setPhone(userUpdateRequest.getPhone());
        updateUser.setEmail(userUpdateRequest.getEmail());
        updateUser.setAvatar(userUpdateRequest.getAvatar());

        boolean result = userService.updateById(updateUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion

    // region 化妆师入驻申请

    @Resource
    private ArtistapplicationService artistapplicationService;

    /**
     * 申请成为化妆师
     */
    @PostMapping("/artist/apply")
    @Operation(summary = "申请成为化妆师", description = "用户申请成为化妆师（需管理员审核）")
    public BaseResponse<Long> applyArtist(@RequestBody ArtistApplyRequest request,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        Long applicationId = artistapplicationService.apply(loginUser.getId(), request);
        return ResultUtils.success(applicationId);
    }

    /**
     * 查询我的申请状态
     */
    @GetMapping("/artist/apply/status")
    @Operation(summary = "查询申请状态", description = "查询当前用户的化妆师入驻申请状态")
    public BaseResponse<ArtistApplicationVO> getApplicationStatus(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ArtistApplicationVO vo = artistapplicationService.getMyApplicationStatus(loginUser.getId());
        return ResultUtils.success(vo);
    }

    // endregion
}
