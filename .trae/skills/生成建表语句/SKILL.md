---
name: 生成建表语句
description: 根据Jimmer框架的Kotlin实体类生成对应的建表语句
---

1. 表主键类型为：bigint unsigned，不是自增的主键，主键值由程序生成。
2. 根据 Schema 注解填充表注释、字段注释。简短说明既可，可以忽略注解中example的内容。
3. 有Column注解指定字段别名时DDL中使用其值。
4. 有org.babyfish.jimmer.sql.Key的字段为唯一索引字段。
5. 生成的语句符合mariadb数据库的语法与规范。
6. 生成日期类型字段时，默认使用 DATETIME(3) 类型，毫秒精度，而不是TIMESTAMP类型。
    1. created_time 字段默认当前时间
    2. updated_time 字段默认当前时间，每次更新时自动设置为当前时间
7. 根据kotlin语言的可空性决定对应字段是否可空