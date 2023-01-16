package dto

import MyModuleBuilder
import com.intellij.openapi.Disposable

class MyModuleContext(
    val moduleBuilder: MyModuleBuilder,
    val projectInfo: ProjectInfo,
    val parentDisposable: Disposable
)