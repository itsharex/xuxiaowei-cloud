# Git 命令

## Git仓库大文件删除

```shell
# 列出 10 个大文件
git rev-list --all | xargs -rL1 git ls-tree -r --long | sort -uk3 | sort -rnk4 | head -10
# 删除文件
git filter-branch --tree-filter "rm -f 要删除的文件路径" -- --all
# 推送
git push -f --all
# 重新检出代码，查看文件是否已被删除
```