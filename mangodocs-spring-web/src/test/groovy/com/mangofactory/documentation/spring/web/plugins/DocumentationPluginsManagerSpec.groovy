package com.mangofactory.documentation.spring.web.plugins

import com.mangofactory.documentation.builder.OperationBuilder
import com.mangofactory.documentation.builder.ParameterBuilder
import com.mangofactory.documentation.spi.DocumentationType
import com.mangofactory.documentation.spi.service.DocumentationPlugin
import com.mangofactory.documentation.spi.service.OperationBuilderPlugin
import com.mangofactory.documentation.spi.service.ParameterBuilderPlugin
import com.mangofactory.documentation.spi.service.ResourceGroupingStrategy
import com.mangofactory.documentation.spi.service.contexts.OperationContext
import com.mangofactory.documentation.spi.service.contexts.ParameterContext
import com.mangofactory.documentation.spring.web.SpringGroupingStrategy
import com.mangofactory.documentation.spring.web.mixins.ServicePluginsSupport
import spock.lang.Specification

@Mixin(ServicePluginsSupport)
class DocumentationPluginsManagerSpec extends Specification {
  def "default documentation plugin always exists" () {
    given:
      def sut = defaultWebPlugins()
    expect:
      sut.documentationPlugins.size() == 0
      sut.documentationPlugins().size() == 1
  }

  def "Resource grouping strategy is defaulted to use SpringResourceGroupingStrategy" () {
    given:
      def sut = defaultWebPlugins()
    expect:
      sut.resourceGroupingStrategy(DocumentationType.SPRING_WEB) instanceof SpringGroupingStrategy
      sut.resourceGroupingStrategy(DocumentationType.SWAGGER_12) instanceof SpringGroupingStrategy
  }



  def "When documentation plugins are explicitly defined" () {
    given:
      def mockPlugin = Mock(DocumentationPlugin)
    and:
      def sut = customWebPlugins([mockPlugin])
    expect:
      sut.documentationPlugins.size() == 1
      sut.documentationPlugins().first() == mockPlugin
  }

  def "When resource grouping strategy has been defined" () {
    given:
      def mockStrategy = Mock(ResourceGroupingStrategy)
    and:
      def sut = customWebPlugins([], [mockStrategy])
      mockStrategy.supports(_) >> true
    expect:
      sut.resourceGroupingStrategy(DocumentationType.SPRING_WEB) == mockStrategy
      sut.resourceGroupingStrategy(DocumentationType.SWAGGER_12) == mockStrategy
  }

  def "Even when no operation plugins are applied an empty operation is returned" () {
    given:
      def operationContext = Mock(OperationContext)
    and:
      operationContext.operationBuilder() >> new OperationBuilder()
    when:
      def sut = customWebPlugins()
      def operation = sut.operation(operationContext)
    then:
      operation != null
  }

  def "Operation plugins are applied" () {
    given:
      def operationPlugin = Mock(OperationBuilderPlugin)
      def operationContext = Mock(OperationContext)
    and:
      operationContext.operationBuilder() >> new OperationBuilder()
      operationPlugin.supports(_) >> true
    when:
      def sut = customWebPlugins([], [], [operationPlugin])
      def operation = sut.operation(operationContext)
    then:
      operation != null
      operationPlugin.apply(operationContext)
  }

  def "Even when no parameter plugins are applied an empty operation is returned" () {
    given:
      def paramContext = Mock(ParameterContext)
    and:
      paramContext.parameterBuilder() >> new ParameterBuilder()
    when:
      def sut = customWebPlugins()
      def parameter = sut.parameter(paramContext)
    then:
      parameter != null
  }

  def "Parameter plugins are applied" () {
    given:
      def paramPlugin = Mock(ParameterBuilderPlugin)
      def paramContext = Mock(ParameterContext)
    and:
      paramContext.parameterBuilder() >> new ParameterBuilder()
      paramPlugin.supports(_) >> true
    when:
      def sut = customWebPlugins([], [], [], [paramPlugin])
      def parameter = sut.parameter(paramContext)
    then:
      parameter != null
      paramPlugin.apply(paramContext)
  }
}