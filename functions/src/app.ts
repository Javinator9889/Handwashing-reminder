const createError = require('http-errors');
const apiRouter = require('./routes/api');
const express = require('express');
const logger = require('morgan');
const path = require('path');

const app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'pug');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));

app.use(apiRouter);

// catch 404 and forward to error handler
app.use((req, res, next) => next(createError(404)));

// error handler
app.use((err, req, res, next) => {
  res.locals.message = err.message;
  res.locals.error = res.app.get('env') === 'development' ? err :  {};

  res.status(err.status || 500);
  res.render('error');
});

export const expressApp = app;
