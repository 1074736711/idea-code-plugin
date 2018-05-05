/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection

import com.intellij.codeInspection.InspectionToolProvider

/**
 * @author max
 */
class CheckProvider : InspectionToolProvider {

  override fun getInspectionClasses(): Array<Class<*>> {
    return arrayOf(
      CodeFormatInspection::class.java,
      PropertyClassCreateInspection::class.java
    )
  }
}