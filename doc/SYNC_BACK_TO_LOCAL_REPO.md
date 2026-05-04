# 同步当前工作区改动回本地仓库

说明：我无法直接写入你本地电脑的工作目录；我已在此工作区生成“可导入快照包”，你在本地仓库按以下步骤覆盖即可将本次改动落到你的 `main`。

## 1) 获取快照包

工作区将生成：
- `/workspace/v51_workspace_snapshot_2026-05-03.tar.gz`
- `/workspace/v51_workspace_snapshot_2026-05-03.sha256`

你把这两个文件下载到本地即可。

## 2) 覆盖到本地仓库

在你的本地仓库根目录执行（会覆盖同名文件）：

```bash
sha256sum -c /path/to/v51_workspace_snapshot_2026-05-03.sha256
tar -xzf /path/to/v51_workspace_snapshot_2026-05-03.tar.gz -C .
```

## 3) 提交到 main（建议按分组提交）

```bash
git status
git add -A
git commit -m "feat: v51 spec implementation baseline"
```

如果你想按“方案 A”拆分成多次提交，我可以把改动分组清单补充到这里，然后你依次分组 `git add -p` 提交。

