/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlTag
import org.jetbrains.kotlin.idea.refactoring.getLineCount

/**
 * Created by 吴昊 on 18-4-26.
 */
open class VueCodeFormatVisitor(holder: ProblemsHolder) : BaseCodeFormatVisitor(holder) {

  override fun visitElement(element: PsiElement) {
    checkFileLength(element)
  }

  private fun checkFileLength(element: PsiElement) {
    if (element is XmlTag) {
      if (element.parent is XmlDocument) {
        when (element.name) {
          "template" -> {
            if (element.getLineCount() > MAX_TEMPLATE_LINES) {
              holder.registerProblem(element, "template长度不得超过${MAX_TEMPLATE_LINES}行")
            }
          }
          "script" -> {
          }
          "style" -> {
          }
        }
      } else {
      }
    }
  }

  companion object {
    const val MAX_TEMPLATE_LINES = 150
    const val DIRECTIVE_PREFIX = "v-"
    const val ACTION_PREFIX = "@"
    const val CUSTOM_ATTR_PREFIX = ":"
  }
}
