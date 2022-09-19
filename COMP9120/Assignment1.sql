CREATE TABLE Transaction_History
(
    transactionId INTEGER,
    date VARCHAR(20),
    PRIMARY KEY(transactionId)
);

CREATE TABLE ETF
(
    code VARCHAR(20) UNIQUE,
    name VARCHAR(20) NOT NULL,
    miniAmount INTEGER,
    description VARCHAR(200),
    establishDate VARCHAR(20),
    unitsNumber INTEGER,
    closingPrice FLOAT,
    category VARCHAR(20),

    PRIMARY KEY (code)
);

CREATE TABLE Person
(
    pId INTEGER,
    fullName VARCHAR(20) NOT NULL,
    address VARCHAR(100),
    email VARCHAR(40),
    password VARCHAR(20) NOT NULL,
    loginName VARCHAR(20) UNIQUE NOT NULL,

    PRIMARY KEY(pId)
);

CREATE TABLE Administrator
(
    pId INTEGER,
    renumeration INTEGER NOT NULL,
    
    FOREIGN KEY(pId) REFERENCES Person(pId),
    PRIMARY KEY(pId)
);

CREATE TABLE Customer
(
    pId INTEGER,
    mobileNo VARCHAR(20),
    cashBalance FLOAT NOT NULL,
    adminId INTEGER,
    PortfolioAmount INTEGER NOT NULL,

    FOREIGN KEY(pId) REFERENCES Person(pId),
    FOREIGN KEY(adminId) REFERENCES Administrator(pId),
    PRIMARY KEY(pId)
);

CREATE TABLE Investment
(
    investmentId INTEGER,
    code VARCHAR(20) UNIQUE,
    pId INTEGER,
    endDate VARCHAR(20),
    reminder INTEGER NOT NULL,
    amount INTEGER NOT NULL,
    frequency INTEGER NOT NULL,

    FOREIGN KEY (investmentId) REFERENCES Transaction_History(transactionId),
    FOREIGN KEY (pId) REFERENCES Customer(pId),
    FOREIGN Key (code) REFERENCES ETF(code),
    PRIMARY KEY (investmentId, code, pId)
);

CREATE TABLE TotalPortfolio
(
    pId INTEGER,
    code VARCHAR(20) UNIQUE,
    amount INTEGER NOT NULL,

    FOREIGN KEY (pId) REFERENCES Customer(pId),
    FOREIGN KEY (code) REFERENCES ETF(code),
    PRIMARY KEY (code, pId)
);

CREATE TABLE Trade
(
    transactionId INTEGER,
    pId INTEGER,
    ETFCode VARCHAR(20) UNIQUE,
    type VARCHAR(20),
    ETFNumber INTEGER,
    ETFPrice FLOAT, 
    finalAmount INTEGER,
    bokerageFee FLOAT,
    duration INTEGER,

    FOREIGN KEY (transactionId) REFERENCES Transaction_History(transactionId),
    FOREIGN KEY (pId) REFERENCES Customer(pId),    
    FOREIGN KEY (ETFCode) REFERENCES ETF(code),
    PRIMARY KEY (transactionId, pId, ETFCode)
);

CREATE TABLE Deposit
(
    pId INTEGER,
    transactionId INTEGER,
    amount INTEGER NOT NULL,

    FOREIGN KEY (pId) REFERENCES Person(pId),
    FOREIGN Key (transactionId) REFERENCES Transaction_History(transactionId),
    PRIMARY KEY (transactionId, pId)
);