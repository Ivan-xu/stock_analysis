# GitHub推送说明

## 🔐 认证方式选择（选择一种）

### 方式1: 使用Personal Access Token（推荐）

1. **创建Personal Access Token**
   - 访问 https://github.com/settings/tokens
   - 点击 "Generate new token" → "Generate new token (classic)"
   - 勾选 `repo` 权限
   - 点击生成，复制token保存（只显示一次！）

2. **推送时使用token**
   ```bash
   cd /Users/simon/trae_solo/stock_v2
   git push -u origin master
   ```
   - 当提示输入密码时，粘贴你的token（不是GitHub密码！）

---

### 方式2: 使用SSH（更方便）

1. **检查是否已有SSH密钥**
   ```bash
   ls -la ~/.ssh
   ```
   
2. **如果没有，生成新密钥**
   ```bash
   ssh-keygen -t ed25519 -C "your-email@example.com"
   # 一路回车使用默认值
   ```

3. **复制公钥**
   ```bash
   cat ~/.ssh/id_ed25519.pub
   ```

4. **添加到GitHub**
   - 访问 https://github.com/settings/keys
   - 点击 "New SSH key"
   - 粘贴公钥内容

5. **更改仓库URL为SSH**
   ```bash
   git remote set-url origin git@github.com:Ivan-xu/stock_analysis.git
   ```

6. **推送代码**
   ```bash
   git push -u origin master
   ```

---

## 🚀 快速操作（推荐方式2）

如果你已经配置了SSH密钥，直接运行：
```bash
cd /Users/simon/trae_solo/stock_v2
git remote set-url origin git@github.com:Ivan-xu/stock_analysis.git
git push -u origin master
```
