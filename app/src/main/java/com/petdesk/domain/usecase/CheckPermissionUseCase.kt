package com.petdesk.domain.usecase

import com.petdesk.domain.model.PermissionItem
import com.petdesk.domain.model.PermissionState
import com.petdesk.domain.model.PermissionType
import com.petdesk.domain.repository.PermissionRepository
import javax.inject.Inject

/**
 * 检查指定权限状态的用例
 */
class CheckPermissionUseCase @Inject constructor(
    private val permissionRepository: PermissionRepository
) {
    operator fun invoke(permissionType: PermissionType): PermissionItem {
        return permissionRepository.checkPermissionStatus(permissionType)
    }
}

/**
 * 检查所有权限状态的用例
 */
class CheckAllPermissionsUseCase @Inject constructor(
    private val permissionRepository: PermissionRepository
) {
    operator fun invoke(): PermissionState {
        return permissionRepository.checkAllPermissionsState()
    }
}

/**
 * 获取需要手动授权的权限列表的用例
 */
class GetPermissionsNeedingManualGrantUseCase @Inject constructor(
    private val permissionRepository: PermissionRepository
) {
    operator fun invoke(): List<PermissionType> {
        return permissionRepository.getPermissionsNeedingManualGrant()
    }
}

/**
 * 获取运行时权限数组的用例
 */
class GetRuntimePermissionsUseCase @Inject constructor(
    private val permissionRepository: PermissionRepository
) {
    operator fun invoke(permissions: List<PermissionType>): Array<String> {
        return permissionRepository.requestRuntimePermissions(permissions)
    }
}
