<g:set var="outcome" value="${log.source == parent}"/>
<g:set var="relatedResourceName" value="${outcome ? log.targetName : log.sourceName}"/>

<div class="item" title="${outcome ? "Исходящий" : "Входящий"}">
  <g:if test="${outcome}">
    <i class="red arrow up icon"></i>
  </g:if>
  <g:else>
    <i class="green arrow down icon"></i>
  </g:else>

  <div class="content">
    <div class="header">
      ${log.value} ${log.target ? log.target.type.measure : log.source ? log.source.type.measure : ""}
    </div>

    <div class="description"><g:render template="/shared/date" model="[date: log.date]"/></div>
    ${outcome ? "Получатель" : "Отправитель"}: ${relatedResourceName}

    <g:if test="${log.comment}">
      <div class="comment">Комментарий: ${log.comment}</div>
    </g:if>
  </div>
</div>