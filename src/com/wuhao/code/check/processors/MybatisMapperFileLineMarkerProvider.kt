/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.processors

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.roots.JavaProjectRootsUtil
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlTag
import org.jetbrains.kotlin.idea.refactoring.toPsiFile
import java.io.File


/**
 * 为Mybatis的mapper配置文件提供跳转至对应的Mapper接口类的gutter
 * @author 吴昊
 * @since 1.1
 */
class MybatisMapperFileLineMarkerProvider : RelatedItemLineMarkerProvider() {

  override fun collectNavigationMarkers(element: PsiElement,
                                        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
    val mapperInfo = resolveMapperInfo(element)
    if (mapperInfo != null && mapperInfo.className.isNotBlank()) {
      val project = element.project
      val sourceRoots = JavaProjectRootsUtil.getSuitableDestinationSourceRoots(project)
      val psiManager = PsiManager.getInstance(project)
      sourceRoots.forEach {
        val classFile = findSourceFile(it, mapperInfo)
        if (classFile != null) {
          val psiFile = psiManager.findFile(classFile) ?: classFile.toPsiFile(project)
          if (psiFile != null) {
            if (psiFile is PsiJavaFile && psiFile.classes.size == 1) {
              val clazz = psiFile.classes[0]
              if (clazz.isInterface) {
                if (!mapperInfo.isMethod) {
                  val builder = NavigationGutterIconBuilder.create(FILE).setTargets(listOf(clazz.nameIdentifier))
                      .setTooltipText("跳转至接口")
                  result.add(builder.createLineMarkerInfo(element))
                } else {
                  val method = clazz.methods.firstOrNull { it.name == mapperInfo.methodName }
                  if (method != null) {
                    val builder = NavigationGutterIconBuilder.create(FILE).setTargets(listOf(method.nameIdentifier))
                        .setTooltipText("跳转至接口")
                    result.add(builder.createLineMarkerInfo(element))
                  }
                }
              }
            }
          }
          return
        }
      }
    }
  }

  private fun findSourceFile(
      root: VirtualFile,
      mapperInfo: MybatisMapperFileLineMarkerProvider.MapperInfo)
      : VirtualFile? {
    val javaFile = root.findFileByRelativePath(File.separator + mapperInfo.getJavaClasspath())
    return javaFile ?: root.findFileByRelativePath(File.separator
        + mapperInfo.getKotlinClasspath())
  }

  private fun resolveMapperInfo(el: PsiElement): MapperInfo? {
    if (el is XmlTag) {
      if (isMapperTag(el)) {
        val classpath = el.getAttributeValue(MAPPER_NAMESPACE_ATTR_NAME)
        if (classpath != null) {
          return MapperInfo(false, classpath)
        }
      } else if (isMethodTag(el)) {
        val classpath = (el.parent as XmlTag).getAttributeValue(MAPPER_NAMESPACE_ATTR_NAME)
        val methodName = el.getAttributeValue(ID_ATTR_NAME)
        if (classpath != null) {
          return MapperInfo(true, classpath, methodName)
        }
      }
    }
    return null
  }

  private fun isMethodTag(el: PsiElement): Boolean {
    return el is XmlTag && isMapperTag(el.parent)
        && el.name in listOf(UPDATE, INSERT, DELETE, SELECT)
  }

  private fun isMapperTag(el: PsiElement): Boolean {
    return el is XmlTag && el.name == MAPPER_TAG_NAME
  }

  private data class MapperInfo(val isMethod: Boolean, val className: String, val methodName: String? = null) {

    fun getJavaClasspath(): String {
      return className.replace(".", File.separator) + ".java"
    }

    fun getKotlinClasspath(): String {
      return className.replace(".", File.separator) + ".kt"
    }
  }

  companion object {
    val FILE = IconLoader.getIcon("/icons/arrow_up.png")
    const val MAPPER_TAG_NAME = "mapper"
    const val MAPPER_NAMESPACE_ATTR_NAME = "namespace"
    const val UPDATE = "update"
    const val INSERT = "insert"
    const val DELETE = "delete"
    const val SELECT = "select"
    const val ID_ATTR_NAME = "id"
  }
}