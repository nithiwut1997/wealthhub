# Database migrations

Liquibase applies the formatted SQL changesets listed in
`db.changelog-master.yml` in order and rolls them back in reverse order. This
order is important: dependent tables such as `asset_price` and `holding` must
be removed before the `asset` and `portfolio` tables they reference.

Every new changeset must include an explicit Liquibase rollback. A changeset
that creates a table should normally use a targeted rollback such as:

```sql
-- rollback DROP TABLE example_table;
```

The rollback must reverse only the objects created or changed by that
changeset. If a changeset creates an index, constraint, sequence, or other
object independently, its rollback must remove that object as well. Do not use
broad destructive rollback commands such as dropping the schema.

If a migration cannot be reversed safely, document the reason next to the
changeset and use `-- rollback empty` so the lack of a rollback is explicit.
Irreversible changes should be exceptional and reviewed before they are added.
