/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.inspection.fix.vue

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.wuhao.code.check.getWords
import com.wuhao.code.check.insertElementAfter

/**
 * vue组件名称修复
 * @author 吴昊
 * @since 1.3.2
 */
class VueComponentNameFix(obj: JSObjectLiteralExpression) : LocalQuickFixOnPsiElement(obj) {

  override fun getFamilyName(): String {
    return "设置name"
  }

  override fun getText(): String {
    return "设置name"
  }

  override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
    val obj = startElement as JSObjectLiteralExpression
    val name = detectComponentNameFromFile(obj.containingFile)
    val exp = JSChangeUtil.createObjectLiteralPropertyFromText("name: '$name'", obj)
    val el = obj.addAfter(exp, obj.firstChild)
    if (obj.properties.size > 1) {
      el.insertElementAfter(JSChangeUtil.createCommaPsiElement(obj))
    }
  }

  private fun detectComponentNameFromFile(file: PsiFile): String {
    val words = getWords(file.name.split(".")[0])
    return if (words.size > 1) {
      words.joinToString("")
    } else {
      (getWords(file.parent!!.name) + words).joinToString("")
    }
  }

}
