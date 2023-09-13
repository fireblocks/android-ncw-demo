package com.fireblocks.sdkdemo.bl.core.environment

import com.fireblocks.sdkdemo.bl.core.server.HeaderProvider
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager

fun StorageManager.removeEnvironment() {
    EnvironmentProvider.getInstance().removeEnvironment(context, deviceId)
}

fun HeaderProvider.environment(): Environment {
    return EnvironmentProvider.getInstance().environment(context(), deviceId())
}
