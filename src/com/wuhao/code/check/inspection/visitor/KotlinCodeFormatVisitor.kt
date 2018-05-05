/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.inspection.visitor

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.wuhao.code.check.inspection.CodeFormatInspection
import com.wuhao.code.check.inspection.fix.KotlinCommentQuickFix
import org.jetbrains.kotlin.idea.refactoring.getLineCount
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.lexer.KtKeywordToken
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType

/**
 * Created by 吴昊 on 18-4-26.
 */
class KotlinCodeFormatVisitor(holder: ProblemsHolder) : BaseCodeFormatVisitor(holder) {

  private val javaOrKotlinCodeFormatVisitor = JavaOrKotlinCodeFormatVisitor(holder)

  override fun visitElement(element: PsiElement) {
    javaOrKotlinCodeFormatVisitor.visitElement(element)
    when (element) {
      is KtClass -> {
        // class必须添加注释
        classCommentChecker.checkKotlin(element)
      }
      is KtProperty -> {
        // 一等属性必须添加注释
        if (element.parent is KtFile && element.firstChild !is KDoc) {
          holder.registerProblem(element, "一等属性必须添加注释", ProblemHighlightType.GENERIC_ERROR, KotlinCommentQuickFix())
        }
        // data类字段必须添加注释
        if (element.parent != null && element.parent is KtClassBody
            && element.containingClass()!!.isData()
            && element.firstChild !is KDoc) {
          holder.registerProblem(element, "数据类字段必须添加注释", ProblemHighlightType.GENERIC_ERROR, KotlinCommentQuickFix())
        }
      }
      is KtFunction -> {
        // 一等方法必须添加注释
        if (element.parent is KtFile && element.firstChild !is KDoc) {
          holder.registerProblem(element, "一等方法必须添加注释", ProblemHighlightType.GENERIC_ERROR, KotlinCommentQuickFix())
        }
        // 接口方法必须添加注释
        val containingClass = element.containingClass()
        if (containingClass != null && containingClass.isInterface()
            && element.firstChild !is KDoc) {
          holder.registerProblem(element, "接口方法必须添加注释", ProblemHighlightType.GENERIC_ERROR, KotlinCommentQuickFix())
        }
        // 方法长度不能超过指定长度
        if (element.getLineCount() > CodeFormatInspection.MAX_LINES_PER_FUNCTION) {
          holder.registerProblem(element, "方法长度不能超过${CodeFormatInspection.MAX_LINES_PER_FUNCTION}行", ProblemHighlightType.GENERIC_ERROR)
        }
      }
      is KtReferenceExpression -> {
        // 使用日志输入代替System.out
        if (element.text == "println") {
          holder.registerProblem(element, "使用日志向控制台输出", ProblemHighlightType.GENERIC_ERROR)
        }
      }
      is LeafPsiElement -> {
        // 检查变量名称，不得少于2个字符
        if ((element.text == "val" || element.text == "var")
            && element.elementType is KtKeywordToken) {
          val paramNameLength = element.nextSibling?.nextSibling?.text?.length
          if (paramNameLength != null && paramNameLength <= 1) {
//            holder.registerProblem(element.nextSibling.nextSibling, "变量名称不得少于两个字符", ProblemHighlightType.ERROR)
          }
        }
        // Kotlin中不需要使用分号
        if (element.text == ";") {
          holder.registerProblem(element, "Kotlin中代码不需要以;结尾", ProblemHighlightType.GENERIC_ERROR, object : LocalQuickFix {

            override fun getFamilyName(): String {
              return "删除分号"
            }

            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
              descriptor.psiElement.delete()
            }
          })
        }

      }
      is KtConstantExpression -> {
        // 不能使用未声明的数字作为参数
        if (element.parent is KtValueArgument
            && element.parent.getChildOfType<KtValueArgumentName>() == null
            && element.text != "0"
            && element.text.matches("\\d+".toRegex())) {
          holder.registerProblem(element, "不得直接使用未经声明的数字作为变量", ProblemHighlightType.ERROR)
        }
      }
    }
  }

}