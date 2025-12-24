# SMP-Economy

**Advanced Economy Plugin for SMP Servers** - A professional, production-ready economy system with banking, auction house, and multi-language support.

## 🌟 Features

- **Diamond Currency System**: Uses diamonds as the primary currency
- **Advanced Banking**: Interest-based banking system with limits
- **Auction House**: Full-featured auction house with bidding system
- **Multi-Language Support**: English + Persian with easy customization
- **Automatic Database Setup**: Zero manual configuration required
- **Multi-Version Compatibility**: Works on Minecraft 1.21 - 1.21.10
- **Performance Optimized**: Designed for 500+ concurrent players
- **Security First**: Anti-exploit protection and input validation
- **Vault Integration**: Compatible with all Vault-based plugins
- **PlaceholderAPI Support**: Rich placeholder system for other plugins

## 🚀 Quick Start Guide

1. **Download** the latest SMP-Economy.jar file
2. **Place** the jar file in your server's `plugins` folder
3. **Restart** your server to generate configuration files
4. **Configure** as needed (optional - works out of the box)
5. **Enjoy** your fully functional economy system!

## 📋 Command Reference

### Economy Commands
- `/eco` or `/economy` - Open main economy GUI
- `/eco balance` or `/bal` - Check your balance
- `/eco pay <player> <amount>` - Send money to another player
- `/eco top` - View top balances
- `/eco reset <player>` - Reset player's balance (admin only)

### Bank Commands
- `/bank` - Open bank interface
- `/bank deposit <amount>` - Deposit money to bank
- `/bank withdraw <amount>` - Withdraw money from bank
- `/bank balance` - Check bank balance

### Auction House Commands
- `/ah` - Open auction house
- `/ah create` - Create a new auction
- `/ah my` - View your auctions
- `/ah bids` - View your bids

### Admin Commands
- `/eco admin` - Open admin dashboard
- `/eco reload` - Reload configuration
- `/eco give <player> <amount>` - Give money to player (admin only)
- `/eco set <player> <amount>` - Set player's balance (admin only)

## 🔐 Permission Reference

| Permission Node | Description |
|----------------|-------------|
| `smpeconomy.eco` | Use economy commands |
| `smpeconomy.eco.balance` | Check balance |
| `smpeconomy.eco.pay` | Send money to other players |
| `smpeconomy.eco.top` | View top balances |
| `smpeconomy.eco.reset` | Reset player balances (admin) |
| `smpeconomy.bank` | Use bank commands |
| `smpeconomy.bank.deposit` | Deposit to bank |
| `smpeconomy.bank.withdraw` | Withdraw from bank |
| `smpeconomy.auction` | Use auction house |
| `smpeconomy.auction.create` | Create auctions |
| `smpeconomy.admin` | Admin commands |
| `smpeconomy.admin.give` | Give money (admin) |
| `smpeconomy.admin.set` | Set balances (admin) |
| `smpeconomy.admin.reload` | Reload config (admin) |

## 🏦 Banking System

The banking system allows players to store money safely with interest:
- **Interest Rate**: Configurable percentage per day
- **Deposit Limits**: Daily and weekly limits to prevent abuse
- **Security**: Separate bank balance from main balance
- **Automatic Interest**: Applied daily at server time

## 🏪 Auction House

Fully featured auction house system:
- **Item Auctions**: Sell any item to other players
- **Bidding System**: Competitive bidding with automatic wins
- **Categories**: Organized by item type for easy searching
- **Duration Options**: Set auction length (1h, 6h, 24h, 7d)
- **Fee System**: Configurable auction fees to control inflation

## 🌍 Multi-Language Support

Built-in support for multiple languages:
- **English (en_US)**: Default language
- **Persian (fa_IR)**: Full Persian translation
- **Easy Translation**: Add your own languages easily
- **Runtime Reload**: Change languages without restart

## 📊 PlaceholderAPI Support

Available placeholders for other plugins:
- `%smpeconomy_balance%` - Player's current balance
- `%smpeconomy_bank_balance%` - Player's bank balance
- `%smpeconomy_total_wealth%` - Total of balance + bank balance
- `%smpeconomy_rank%` - Player's economy rank

## ⚙️ Configuration

### Economy Settings (`config.yml`)
```yaml
# Default starting balance for new players
starting_balance: 100.0

# Maximum balance a player can have
max_balance: 1000000000.0

# Minimum amount for transactions
min_transaction: 0.01

# Transaction fee percentage (0.0 = no fee)
transaction_fee: 0.0

# Enable/disable economy
enabled: true
```

### Banking Settings (`bank.yml`)
```yaml
# Enable banking system
enabled: true

# Daily deposit limit
daily_deposit_limit: 1000000.0

# Weekly deposit limit
weekly_deposit_limit: 10000000.0

# Daily withdrawal limit
daily_withdrawal_limit: 1000000.0

# Interest rate percentage per day
interest_rate: 0.1

# Minimum balance required to earn interest
min_balance_for_interest: 1000.0
```

## 🔧 Performance Optimization

- **Caching System**: Player data cached in memory
- **Async Database**: Non-blocking database operations
- **Efficient Queries**: Optimized SQL with proper indexing
- **Resource Management**: Proper cleanup of connections and resources

## 🛡️ Security Features

- **Input Validation**: All user input is sanitized
- **Anti-Exploit**: Protection against common exploits
- **Rate Limiting**: Prevents spam and abuse
- **Transaction Logging**: Complete audit trail
- **Secure Storage**: Safe handling of player data

## 🔄 Updates and Maintenance

- **Auto-Update**: Built-in update checking
- **Schema Migration**: Automatic database updates
- **Backup System**: Automatic database backups
- **Rollback Support**: Safe rollback capabilities

## 🤝 API for Developers

SMP-Economy provides a robust API for other plugin developers:

```java
// Get economy instance
SMP_Economy economy = (SMP_Economy) Bukkit.getPluginManager().getPlugin("SMP-Economy");

// Check player balance
double balance = economy.getEconomyManager().getBalance(player);

// Add money to player
economy.getEconomyManager().addMoney(player, 100.0);

// Transfer money between players
economy.getTransferManager().transferMoney(sender, receiver, 50.0);
```

## 🐛 Troubleshooting

### Common Issues

1. **Plugin doesn't load**
   - Check server version compatibility
   - Verify Java version (requires Java 21+)
   - Check console for error messages

2. **Database connection fails**
   - Verify database credentials in config
   - Ensure database server is running
   - Check firewall settings

3. **Commands not working**
   - Ensure you have proper permissions
   - Check if plugin loaded successfully
   - Verify command aliases in config

### Support

- **Issues**: Report on GitHub
- **Discord**: Join our support server
- **Email**: support@smpeconomy.com

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Thanks to the SpigotMC community for support
- Special thanks to contributors and beta testers
- Inspired by the needs of SMP server communities worldwide