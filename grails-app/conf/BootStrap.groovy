import org.quartz.JobBuilder
import org.springframework.security.acls.domain.ObjectIdentityImpl
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder as SCH
import ru.srms.larp.platform.GameAclService
import ru.srms.larp.platform.game.Game
import ru.srms.larp.platform.game.character.GameCharacter
import ru.srms.larp.platform.game.mail.LetterContent
import ru.srms.larp.platform.game.mail.LetterRef
import ru.srms.larp.platform.game.mail.LetterType
import ru.srms.larp.platform.game.mail.MailBox
import ru.srms.larp.platform.game.news.NewsFeed
import ru.srms.larp.platform.game.news.NewsItem
import ru.srms.larp.platform.game.resources.GameResource
import ru.srms.larp.platform.game.resources.PeriodicResourceUpdateJob
import ru.srms.larp.platform.game.resources.ResourceInstance
import ru.srms.larp.platform.game.resources.ResourceOrigin
import ru.srms.larp.platform.game.roles.CharacterRole
import ru.srms.larp.platform.game.roles.GameRole
import ru.srms.larp.platform.sec.SpringRole
import ru.srms.larp.platform.sec.SpringUser
import ru.srms.larp.platform.sec.SpringUserSpringRole

import static org.springframework.security.acls.domain.BasePermission.*

class BootStrap {

  def aclService
  def aclUtilService
  def quartzScheduler
  def grailsApplication
  GameAclService gameAclService
  def init = { servletContext ->

    // create a Job for periodic resource update
    if (grailsApplication.config.grails.larp.platform.clearQuartzResourceTasks) {
      println "Clearing quartz tasks..."
      if(quartzScheduler.checkExists(PeriodicResourceUpdateJob.jobKey)) {
        quartzScheduler.deleteJob(PeriodicResourceUpdateJob.jobKey)
      }
    }

    def resourceUpdateJob = JobBuilder.newJob(PeriodicResourceUpdateJob.class)
        .withIdentity(PeriodicResourceUpdateJob.jobKey)
        .storeDurably()
        .build()
    quartzScheduler.addJob(resourceUpdateJob, true)

    // create basic roles
    if(SpringRole.countByAuthority('ROLE_ADMIN') == 0)
      new SpringRole(authority: 'ROLE_ADMIN').save(flush: true)
    if(SpringRole.countByAuthority('ROLE_GM') == 0)
      new SpringRole(authority: 'ROLE_GM').save(flush: true)
    if(SpringRole.countByAuthority('ROLE_ACL_CHANGE_DETAILS') == 0)
      new SpringRole(authority: 'ROLE_ACL_CHANGE_DETAILS').save(flush: true)

    // create admin
    if (SpringUser.countByUsername('admin') == 0) {
      def roleAdmin = SpringRole.findByAuthority('ROLE_ADMIN')
      def roleAclChanger = SpringRole.findByAuthority('ROLE_ACL_CHANGE_DETAILS')
      def admin = new SpringUser(username: 'admin', name: 'Admin',
          password: grailsApplication.config.grails.larp.platform.adminInitialPassword,
          email: 'admin@larp.srms.ru')
          .save()
      SpringUserSpringRole.create(admin, roleAdmin)
      SpringUserSpringRole.create(admin, roleAclChanger)
    }

    // populate DB with temporary data for testing and development
    if (grailsApplication.config.grails.larp.platform.setupTestData) {
      tempInit(servletContext)
    }
  }

  def tempInit(def servletContext) {
    def roleGm = SpringRole.findByAuthority('ROLE_GM')
    def roleAclChanger = SpringRole.findByAuthority('ROLE_ACL_CHANGE_DETAILS')

    def gm1 = new SpringUser(username: "gm1", name: "Game Master 1", password: "a", email: 'gm1@larp.srms.ru').save()
    SpringUserSpringRole.create(gm1, roleGm)
    SpringUserSpringRole.create(gm1, roleAclChanger)
    def gm2 = new SpringUser(username: "gm2", name: "Game Master 2", password: "a", email: 'gm2@larp.srms.ru').save()
    SpringUserSpringRole.create(gm2, roleGm)
    SpringUserSpringRole.create(gm2, roleAclChanger)

    def usr1 = new SpringUser(username: "usr1", name: "User 1", password: "a", email: 'usr1@larp.srms.ru').save()
    def usr2 = new SpringUser(username: "usr2", name: "User 2", password: "a", email: 'usr2@larp.srms.ru').save()

    Game game1 = new Game(title: "Красная шапочка", alias: "the-red-hat",
        overview: "Игра по знаменитой сказке.", preview: "Игра по знаменитой сказке.")
        .addToMasters(gm1)
        .addToModules(Game.GameModule.MAIL)
        .save()

    // have to be authenticated as an admin to create ACLs
    SCH.context.authentication = new UsernamePasswordAuthenticationToken(
        'admin', 'a', AuthorityUtils.createAuthorityList('ROLE_ADMIN'))

    aclUtilService.addPermission(game1, gm1.username, ADMINISTRATION)

    def redHat = new GameCharacter(name: "красная шапочка", game: game1, alias: 'red-hat').save()
    def wolf = new GameCharacter(name: "волк", game: game1, alias: 'wolf').save()
    aclService.createAcl(new ObjectIdentityImpl(redHat))
    aclService.createAcl(new ObjectIdentityImpl(wolf))
    game1.save()

    // TODO проверить - кажется, не сохраняется
    usr1.addToCharacters(wolf).save()
    aclUtilService.addPermission(wolf, usr1.username, READ)
    aclUtilService.addPermission(wolf, usr1.username, WRITE)

    def feed = new NewsFeed(game: game1, title: "Новости леса").save()
    gameAclService.createAcl(feed)

    def now = new Date().getTime()
    new NewsItem(feed: feed, title: "Новость раз", text: "Все умерли", created: new Date(now - 2400000)).save()

    // game 2
    Game game2 = new Game(title: "Камелот", alias: "CamelotAge77",
        overview: "Игра о славных ряцарях камелота.", preview: "Игра о славных ряцарях камелота.")
    game2.addToMasters(gm1).addToMasters(gm2).save()
    game2
        .addToModules(Game.GameModule.MAIL)
        .addToModules(Game.GameModule.NEWS)
        .addToModules(Game.GameModule.RESOURCES)
        .addToModules(Game.GameModule.REQUEST_FORM)
        .save()

    aclUtilService.addPermission(game2, gm2.username, ADMINISTRATION)
    aclUtilService.addPermission(game2, gm1.username, ADMINISTRATION)

    def king = new GameCharacter(name: "Король Артур", game: game2, player: usr1, alias: 'arthur').save()
    def lancelot = new GameCharacter(name: "Ланселот", game: game2, player: usr2, alias: 'lancelot').save()
    def merlin = new GameCharacter(name: "Мерлин", game: game2, player: usr2, alias: 'merlin').save()

    aclUtilService.addPermission(king, usr1.username, READ)
    aclUtilService.addPermission(king, usr1.username, WRITE)
    aclUtilService.addPermission(lancelot, usr2.username, READ)
    aclUtilService.addPermission(lancelot, usr2.username, WRITE)
    aclUtilService.addPermission(merlin, usr2.username, READ)
    aclUtilService.addPermission(merlin, usr2.username, WRITE)

    game2.save()

    // новости
    feed = new NewsFeed(game: game2, title: "Круглый стол").save()
    gameAclService.createAcl(feed)


    now = new Date().getTime()
    new NewsItem(feed: feed, title: "Новость раз", text: "Все хорошо", created: new Date(now - 2400000)).save()
    new NewsItem(feed: feed, title: "Новость два", text: "Все плохо", created: new Date(now - 3600000)).save()
    new NewsItem(feed: feed, title: "Новость три", text: "Все норм", created: new Date(now - 1200000)).save()

    def mFeed = new NewsFeed(game: game2, title: "Новости магии").save()
    gameAclService.createAcl(mFeed)

    new NewsItem(feed: mFeed, title: "Новость магии раз", text: "Все хорошо у магов").save()
    new NewsItem(feed: mFeed, title: "Новость магии два", text: "Все хорошо!").save()
    new NewsItem(feed: mFeed, title: "Новость магии три", text: "Все хорошо!!! Дада.").save()

    // почта
    def mailbox = new MailBox(game: game2, address: "test@camelot.com").save()
    gameAclService.createAcl(mailbox)
    def mailbox2 = new MailBox(game: game2, address: "test2@camelot.com").save()
    gameAclService.createAcl(mailbox2)
    def mailbox3 = new MailBox(game: game2, address: "test3@camelot.com").save()
    gameAclService.createAcl(mailbox3)

    def m1 = new LetterContent(subject: "Хай", text: "Как оно?", sender: mailbox, recipients: [mailbox2], time: new Date()).save()
    new LetterRef(mailbox: mailbox, content: m1, type: LetterType.OUTGOING).save()
    new LetterRef(mailbox: mailbox2, content: m1, type: LetterType.INBOX).save()

    def m2 = new LetterContent(subject: "Хай трэш", text: "Как оно?!", sender: mailbox, recipients: [mailbox3], time: new Date()).save()
    new LetterRef(mailbox: mailbox, content: m2, type: LetterType.TRASH).save()
    new LetterRef(mailbox: mailbox2, content: m2, type: LetterType.INBOX).save()

    def m3 = new LetterContent(subject: "Сам Хай", text: "Каконо(", sender: mailbox2, recipients: [mailbox], time: new Date()).save()
    new LetterRef(mailbox: mailbox2, content: m3, type: LetterType.OUTGOING).save()
    new LetterRef(mailbox: mailbox, content: m3, type: LetterType.INBOX).save()

    def m4 = new LetterContent(subject: "Хай драфт", text: "Как оно а?!", sender: mailbox, recipients: [mailbox2, mailbox3], time: new Date()).save()
    new LetterRef(mailbox: mailbox, content: m4, type: LetterType.DRAFT).save()

    // ресурсы
    def money = new GameResource(title: "Деньги", storage: 'Счет', measure: "руб", game: game2, identifierTitle: "Счет #").save()
    new GameResource(title: "Кровь", storage: 'Пакет', measure: "л.", game: game2, identifierTitle: "Карта донора").save()
    new GameResource(game: game2, title: "Мана", storage: 'Маг', measure: "ед", identifierTitle: "").save()
    def bank = new ResourceOrigin(resource: money, title: "Банк Ололоево").save()
    new ResourceOrigin(resource: money, title: "Банк Пурпурово").save()

    def res1 = new ResourceInstance(type: money, identifier: "1455-13345", value: 66.4).save()
    def res2 = new ResourceInstance(type: money, identifier: "666ч666",
        value: 651, owner: lancelot, transferable: true, ownerEditable: true, origin: bank).save()
    gameAclService.createAcl(res1)
    gameAclService.createAcl(res2)
    aclUtilService.addPermission(res2, lancelot.authority, READ)
    aclUtilService.addPermission(res2, lancelot.authority, CREATE)
    aclUtilService.addPermission(res2, lancelot.authority, WRITE)

    // роли
    def r1 = new GameRole(title: "ФСБ", game: game2).save(flush: true)
    new GameRole(title: 'Стажер ФСБ', game: game2, parent: r1).save()
    def r2 = new GameRole(title: 'Офицер ФСБ', game: game2, parent: r1).save()
    new GameRole(title: 'Начальник ФСБ', game: game2, parent: r2).save()

    new GameRole(title: "Вампир", game: game2).save()
    new GameRole(title: "Человек", game: game2).save()
    def werewolf = new GameRole(title: "Оборотень", game: game2).save()

    CharacterRole.create(lancelot, r1)
    CharacterRole.create(lancelot, r2)
    CharacterRole.create(merlin, r1)

    aclUtilService.addPermission(mFeed, r1.authority, READ)
  }


  def destroy = {
  }
}
