<#--
/*
 * $Id: controlheader.ftl,v 1.1 2013/09/23 16:25:43 npiedeloup Exp $
 *
 */
-->
<#include "/${parameters.templateDir}/xhtml/controlheader-core.ftl" />
<#assign currentLayout = controlLayout_type?default('none') />	
<#if currentLayout = 'table'>
	<#assign labelColspan = parameters.labelcolspan?default(1) />
	<td <#t/>
	<#if parameters.inputcolspan??><#t/>
	    colspan="${parameters.inputcolspan?html}"<#t/>	    
	<#t/></#if>
	<#if parameters.align??><#t/>
	    align="${parameters.align?html}"<#t/>
	<#t/></#if>
	><#t/>
	<#if parameters.name?? && fieldErrors?? && fieldErrors[parameters.name]??>
		<#assign previousCssClass = appendedCssClass!''/>
		<#assign appendedCssClass = previousCssClass +' error'/>
	</#if>
	<#if controlLayout_tablecolspan?exists >
    	<#assign columnCount = controlLayout_currentColumnCount + parameters.inputcolspan?default(1) />	
		<#-- update the value of the controlLayout_currentColumnCount bean on the value stack. -->
		${stack.setValue('#controlLayout_currentColumnCount', columnCount)}
	</#if>
</#if>
