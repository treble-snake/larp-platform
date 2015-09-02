/**
 * External config file
 */

// emulate JNDI resource
def jndiDescriptor = [
    type           : "javax.sql.DataSource",
    auth           : "Container",
    description    : "Data source for LARP Platform",
    driverClassName: "org.postgresql.Driver",
    url            : "jdbc:postgresql://localhost:5432/larp_platform_dev",
    username       : "postgres",
    password       : "postgres",
    maxActive      : "10",
    maxIdle        : "4"
]

/**
 * Environment related settings
 */
environments {

  // for database migrations
  dbmigr {
    // disable native GORM validation (fires before migration)
    dataSource {
      dbCreate = ""
    }

    // workaround for quartz plugin - it starts before migrations and crashes the app
    // if no database has been created yet
    grails.plugin.quartz2.autoStartup = false
    org.quartz = ['jobStore.class': 'org.quartz.simpl.RAMJobStore']

    // workaround for database migration plugin
    grails.naming.entries = [
        "java:comp/env/jdbc/larp_platform_db": jndiDescriptor
    ]
  }

  development {
    dataSource {
//      dbCreate = "create-drop"
    }
    grails.naming.entries = [
        "jdbc/larp_platform_db": jndiDescriptor
    ]
  }

  test {
    dataSource {
      // TODO configure after deploying autotests
    }
  }

  production {
    dataSource {
      // TODO config it properly
    }
  }
}

/**
 * Mail settings
 */
grails.mail.default.from = "no-reply@larp.srms.club"
grails.mail.disabled = true

/**
 * Custom settings
 */
// Пароль для админа (по умолчанию, при создании нового)
grails.larp.platform.adminInitialPassword = "a"

// Заполнять ли базу тестовыми данными
grails.larp.platform.setupTestData = false
// Очищать ли задачи Quartz-a, связанные с обновлением ресурсов
grailsApplication.config.grails.larp.platform.clearQuartzResourceTasks = false