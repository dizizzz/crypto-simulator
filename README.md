# 🪙 Crypto Simulator

[🇺🇦](#ua) | [en](#en)

---

<a id="ua"></a>

## Симулятор криптовалютних транзакцій у замкнутій системі. 💸

Симулятор з власною реалізацією криптографії, валідації, Proof-of-Work та веб-інтерфейсом. Можна задавати параметри запуску, спостерігати за створенням транзакцій і блоків та аналізувати метрики мережі. 🔍

### 🧰 Технології

Java 21 · Maven 3.9 · JUnit 5 · Jackson 2.16 · Javalin 6.4 · Chart.js 4.4

### 🏗 Архітектура

```
api          ← REST API
io           ← JSON серіалізація
simulation   ← Рушій симуляції, метрики
chain        ← Блоки, мемпул, майнер
ledger       ← Стан системи, валідатор
domain       ← Гаманець, транзакція, адреса
crypto       ← SHA-256, ECDSA, підписи
```

### 🔐 Що під капотом

- 🔑 Гаманці з парами ключів ECDSA на кривій secp256r1
- ✍️ Підписи транзакцій через SHA256withECDSA
- 📬 Мемпул з обмеженою місткістю
- ⛏ Майнінг блоків через Proof-of-Work з налаштовуваною складністю
- ⚖️ Закон збереження загальної суми балансів — комісії перерозподіляються майнеру через coinbase

### ▶️ Як запустити

Що знадобиться: Java 21+ і Maven 3.9+.

<details>
<summary>Показати інструкцію 🛠</summary>

1. Клонуй репозиторій:

```bash
   git clone https://github.com/dizizzz/crypto-simulator.git
   cd crypto-simulator
```

2. Зібрати проєкт:

```bash
   mvn clean install
```

3. Запустити тести:

```bash
   mvn test
```

4. Запустити сервер:

```bash
   mvn exec:java -Dexec.mainClass="cryptosim.api.Server"
```

5. Відкрити браузер на `http://localhost:7000`, ввести параметри симуляції та натиснути «Запустити симуляцію». 🚀

</details>

### 📊 Що видно у результатах

- Кількість змайнованих блоків та підтверджених транзакцій
- Сумарна комісія майнера
- Середня латентність підтвердження транзакції
- Середня кількість спроб nonce та час майнінгу
- 📈 Графік розподілу балансів між учасниками
- 📊 Графік кількості транзакцій у кожному блоці

---

<a id="en"></a>

## A cryptocurrency transaction simulator in a closed system. 💸

A simulator with custom implementations of cryptography, validation, Proof-of-Work, and a web interface. You can configure simulation parameters, watch transactions and blocks being created, and analyze network metrics. 🔍

### 🧰 Tech stack

Java 21 · Maven 3.9 · JUnit 5 · Jackson 2.16 · Javalin 6.4 · Chart.js 4.4

### 🏗 Architecture

```
api          ← REST API
io           ← JSON serialization
simulation   ← Simulation engine, metrics
chain        ← Blocks, mempool, miner
ledger       ← World state, validator
domain       ← Wallet, transaction, address
crypto       ← SHA-256, ECDSA, signatures
```

### 🔐 Under the hood

- 🔑 Wallets with ECDSA key pairs on the secp256r1 curve
- ✍️ Transaction signing via SHA256withECDSA
- 📬 Mempool with limited capacity
- ⛏ Block mining via Proof-of-Work with configurable difficulty
- ⚖️ Total balance conservation law — fees are redistributed to the miner via coinbase

### ▶️ How to run

You'll need Java 21+ and Maven 3.9+.

<details>
<summary>Show instructions 🛠</summary>

1. Clone the repository:

```bash
   git clone https://github.com/dizizzz/crypto-simulator.git
   cd crypto-simulator
```

2. Build the project:

```bash
   mvn clean install
```

3. Run the tests:

```bash
   mvn test
```

4. Start the server:

```bash
   mvn exec:java -Dexec.mainClass="cryptosim.api.Server"
```

5. Open `http://localhost:7000` in your browser, set simulation parameters, and click "Run". 🚀

</details>

### 📊 What you see in results

- Number of mined blocks and confirmed transactions
- Total miner fees collected
- Average transaction confirmation latency
- Average nonce attempts and mining time
- 📈 Chart with balance distribution between participants
- 📊 Chart with transaction count per block
