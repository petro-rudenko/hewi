package models
import AppContext._
import net.fwbrasil.activate.migration.Migration


class CreateSchema extends Migration {
    
    def timestamp = System.currentTimeMillis

    def up = {
        createTableForAllEntities.ifNotExists
        table[User].addIndex("username", "username_idx", true).ifNotExists
    }
}