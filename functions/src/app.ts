import * as path from 'path';
import * as logger from 'morgan';
import * as express from 'express';
// import * as admin from 'firebase-admin;'
import * as apiRouter from './routes/api';
import * as createError from 'http-errors';
import * as functions from 'firebase-functions';

export const app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'pug');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({extended: false}));
app.use(express.static(path.join(__dirname, 'public')));

/**
 * -------------
 * Test purposes
 * -------------
 const testUpdater = new updater.Updater(null, null, ['covid-19', 'enfermedad'], functions.config().newsriver.key, 'es');
 app.get('/api', (req, res) =>
 testUpdater.request()
 .then(it => res.json(it))
 );
 * ---------------
 */

// @ts-ignore
app.use(apiRouter);

// catch 404 and forward to error handler
app.use((req, res, next) => next(createError(404)));

export const webApi = functions.https.onRequest(app);

// error handler
app.use((err, req, res, next) => {
  res.locals.message = err.message;
  res.locals.error = res.app.get('env') === 'development' ? err : {};

  res.status(err.status || 500);
  res.render('error');
});
