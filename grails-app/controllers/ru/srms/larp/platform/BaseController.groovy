package ru.srms.larp.platform

import org.springframework.http.HttpStatus
import ru.srms.larp.platform.breadcrubms.AutoDescriptor
import ru.srms.larp.platform.breadcrubms.Breadcrumb
import ru.srms.larp.platform.breadcrubms.Descriptor
import ru.srms.larp.platform.exceptions.AjaxException

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static ru.srms.larp.platform.UrlHelper.composeAttrs
import static ru.srms.larp.platform.UrlHelper.determineMapping

abstract class BaseController {

  public static final String DEFAULT_ACTION = 'index'

  /**
   * Adds breadcrumbs data to the model
   */
  def afterInterceptor = { model, modelAndView ->
    if(modelAndView?.model) {
      def allCrubms = appendRootCrumbs(getCrumbs())
      Collections.reverse(allCrubms)
      modelAndView.model.breadcrumbs = allCrubms
    }
  }

  /**
   * Returns a map of breadcrumb descriptors for controller actions.
   * Should be overridden.
   *
   * @return map of breadcrumb descriptors, empty map by default
   */
  public Map getBreadcrumbDescriptors() { [:] }

  /**
   * Computes maximum of entries per page for pagination
   * @param max max parameter coming from request
   * @return maximum of entries
   */
  protected int pageMax(Integer max) {
    Math.min(max ?: 10, 100)
  }

  /**
   * Composes map with pagination options based on request parameters.<br/>
   * Also sets up params.max value.
   * @return map with offset and max entries for pagination
   */
  protected Map paginator(Integer max = null) {
    params.max = pageMax(params.int('max') ?: max)
    [offset: params.offset ?: 0, max: params.max]
  }

  /**
   * Validates input data for domain object and make redirects if instance is not found or
   * contains errors
   * @param object domain instance object
   * @param view view to redirect to
   * @return {@code true} if validation is successful, {@code false} otherwise
   */
  protected boolean validateData(def object, String view) {
    if (object == null) {
      notFound()
      return false
    }

    object.validate()
    if (view && object.hasErrors()) {
      respond object.getErrors(), view: view
      return false
    }

    return true
  }

  /**
   * Validates input data for domain object and make redirects if instance is not found
   * @param object domain instance object
   * @return {@code true} if validation is successful, {@code false} otherwise
   */
  protected boolean validateData(def object) {
    return validateData(object, null)
  }

  /**
   * @return i18n message code for domain class label
   */

  /**
   * Make appropriate respond if instance was not found
   */
  protected void notFound() {
//        respondChange(message(code: 'default.not.found.message').toString(), NOT_FOUND, null, params.id)
    render status: 404
  }

  /**
   * Make appropriate respond after instance was somehow changed
   * @param message code for information message relevant to performed action
   * @param status http status code
   * @param route information with whereabouts of target redirect; map can contain [action, controller, id, method, params]
   *
   * @return The result of the "request.withFormat" closure call -- not true yet
   */
  protected def respondChange(String message, HttpStatus status, Map route = null) {
    flash.success = message;
    def map = redirectParams(route)
    map.status = status
    redirect map

    // TODO если "удалить" делать ссылкой, то редиректа не происходит. разобраться.
//        request.withFormat {
//            form multipartForm {
//                flash.message = message(code: message, args: [message(code: labelCode()), id ?: object?.id])
//                redirect action: 'index', params: [gameAlias: params.gameAlias], method: "GET"
//            }
//            '*' {
//                if (object)
//                    respond object, [status: status]
//                else
//                    render status: status
//            }
//        }
  }

  /**
   * May be overridden for custom redirect rules
   * @param route - map with navigation rules; can contain [action, controller, id, method, params]
   * @return map of redirect params
   */
  protected Map redirectParams(Map route = null) {
    def defaults = [action: route?.action ?: DEFAULT_ACTION, params: [:], method: route?.method ?: "GET"]
    if (route?.params) defaults.params += route.params
    if (route?.id) defaults.id = route.id
    if (route?.controller) defaults.controller = route.controller
    if (route?.mapping) defaults.mapping = route.mapping

    defaults = determineMapping(composeAttrs(defaults, params), params)
    return defaults
  }

  /**
   * Wrapper for ajax actions (renders errors in appropriate format)
   * @param action action to perform
   */
  protected def doAjax(Closure action) {
    try {
      action()
    } catch (AjaxException e) {
      renderAjaxError(ui.message([type: 'error'], e.getMessage()).toString())
    }
    catch (Exception other) {
      renderAjaxError("Что-то пошло не так: ${other.class.simpleName} (${other.message})")
    }
  }

  // TODO разобраться, почему он все время пытается вызвать эти методы при возникновении ошибок (из-за чего их пришлось сделать public)
  /**
   * Renders an error message for ajax action from exception
   * @param message message it is
   */
  protected def renderAjaxError(String message) {
    response.status = BAD_REQUEST.value()
    render message
  }

  /**
   * Creates list of breadcrumbs for a page
   * @return list of breadcrumbs
   */
  private def getCrumbs() {
    List<Breadcrumb> crumbs = []

    def url = null
    Descriptor descriptor = getParentDescriptor(Descriptor.get([
        controller: AutoDescriptor.deHyphenate(params.controller),
        action    : AutoDescriptor.deHyphenate(params.action)
    ]))

    while (!descriptor.isEmpty()) {
      crumbs.add(new Breadcrumb(getCrumbTitle(descriptor), url))
      if (descriptor.isRoot())
        break;

      url = getCrumbParentLink(descriptor)
      descriptor = getParentDescriptor(descriptor)
    }

    return crumbs
  }

  /**
   * Retrieves parent descriptor of the current descriptor
   * @param current descriptor of the current action
   * @return parent descriptor
   */
  private Descriptor getParentDescriptor(Descriptor current) {
    Map descriptors;
    def parentRoute = current?.modifyParentRoute ?
        current.modifyParentRoute(current.parentRoute, params) : current?.parentRoute

    if (!parentRoute?.controller)
      throw new RuntimeException("No route controller found in current breadcrumb descriptor")

    def artifact = grailsApplication
        .getArtefactByLogicalPropertyName("Controller", parentRoute.controller)
    if (!artifact) return Descriptor.empty()
    def controller = applicationContext.getBean(artifact.clazz.name)
    if (!controller) return Descriptor.empty()
    descriptors = controller.invokeMethod('getBreadcrumbDescriptors', null)

    if (!descriptors)
      return Descriptor.empty()

    def action = parentRoute.action ?: params.action
    Descriptor result = action && descriptors.containsKey(action) ?
        descriptors.get(action) :
        descriptors.get(Descriptor.DEFAULT_KEY) ?: Descriptor.auto(parentRoute ?: params)

    result.route = new LinkedHashMap<>(parentRoute)
    return result
  }

  /**
   * Determines node title
   * @param controller
   * @param action
   * @return
   */
  private String getCrumbTitle(Descriptor descriptor) {
    descriptor.composeTitle ?
        descriptor.composeTitle(params) :
        message(code: "${descriptor.route.controller}.breadcrumb.${descriptor.route.action}",
            default: message(code: "${descriptor.route.controller}.breadcrumb.defaultAction",
                default: message(code: "default.breadcrumb.${descriptor.route.action}", default: 'Крошка-картошка')))
  }

  /**
   * Creates URL to the parent node for a breadcrumb descriptor
   * @param descriptor breadcrumb descriptor
   * @return URL to the parent node
   */
  private String getCrumbParentLink(Descriptor descriptor) {
    if (!descriptor.parentRoute)
      return null;

    def route = descriptor.modifyParentRoute ?
        descriptor.modifyParentRoute(descriptor.parentRoute, params) : descriptor.parentRoute
    return createLink(determineMapping(composeAttrs(route, params), params))
  }

  /**
   * Adds root breadcrumbs (for game and character) to the breadcrumbs list
   * @param crumbs list of current breadcrumbs
   * @return modified list of breadcrumbs
   */
  private def appendRootCrumbs(List<Breadcrumb> crumbs) {
    if (params.character)
      crumbs.add(new Breadcrumb(params.character.name,
          createLink([mapping: 'playAs',
                      params : [charAlias: params.charAlias, gameAlias: params.gameAlias]])))

    if (params.game)
      crumbs.add(
          new Breadcrumb(params.game.title,
              createLink([mapping: 'game', params: [gameAlias: params.gameAlias]])))

    return crumbs
  }
}
