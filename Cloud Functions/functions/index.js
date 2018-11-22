const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const cors = require('cors')({origin: true});
const validator = require('validator');
const TokenGenerator = require('uuid-token-generator');

exports.register = functions.https.onRequest((req, res) => {
	if (req.method === 'PUT') {
		res.status(403).send('Forbidden!');
		return;
	}
	cors(req, res, () => {
		let name = req.query.name;
		//validations
		if (!name) {
			res.status(200).send("Please enter name.");
			return;
		}
		let email = req.query.email;
		if (!email) {
			res.status(200).send("Please enter email.");
			return;
		}
		let password = req.query.password;
		if (!password) {
			res.status(200).send("Please enter password.");
			return;
		}
		if (!validator.isLength(password, 3)) {
			res.status(200).send("Please enter valid password.");
			return;
		}
		if(!validator.isEmail(email)){
			res.status(200).send("Please enter valid email.");
			return;
		}
		
		//check if user already exists in firestore
		var userRef = admin.firestore().collection('users')

		var userExists;
		userRef.where('email', '==', email).get()
		.then(snapshot => {
			userExists = snapshot.size;
			console.log(`user by email query size ${userExists}`);
			//send error if user exists
			if(userExists && userExists > 0){
				res.status(200).send("Account exists with same email Id.");
				return;
			}
			//add user to database
			admin.firestore().collection('users').add({
				name: name,
				email: email,
				password: password
			}).then(ref => {
				console.log('add user account', ref.id);
				res.status(200).send("User account created.");
				return;	
			});      	   	

		})
		.catch(err => {
			console.log('error getting user by email', err);
			res.status(200).send("System error, please try again.");
		});


	});
});
exports.login = functions.https.onRequest((req, res) => {
	if (req.method === 'PUT') {
		res.status(403).send('Forbidden!');
		return;
	}
	cors(req, res, () => {
		let email = req.query.email;
		//validation
		if (!email) {
			res.status(200).send("Please enter email.");
			return;
		}
		if(!validator.isEmail(email)){
			res.status(200).send("Please enter valid email.");
			return;
		}
		let password = req.query.password;
		if (!password) {
			res.status(200).send("Please enter password.");
			return;
		}
		if (!validator.isLength(password, 3)) {
			res.status(200).send("Please enter valid password.");
			return;
		}

		//get password from db and match it with input password
		var userRef = admin.firestore().collection('users')

		userRef.where('email', '==', email).get()
		.then(snapshot => {
			if(snapshot.size > 1){
				res.status(200).send("Invalid account.");
				return;
			}
			snapshot.forEach(doc => {
				console.log(doc.id, '=>', doc.data().name);
				var userPass = doc.data().password;

				//if password matches, generate token, save it in db and send it
				if(userPass && password == userPass){
					const tokgenGen = new TokenGenerator(256, TokenGenerator.BASE62);
					const tokenStr = tokgenGen.generate();

					//save token in db to use for other client request's authentication verification
					var tokenData = { email: doc.data().email};
					admin.firestore().collection('tokens').doc(tokenStr).set(tokenData);

					res.status(200).send("token:"+tokenStr );
				}else{
					res.status(200).send("Invalid email/password.");
				}
			});
		})
		.catch(err => {
			console.log('error getting user by email', err);
			res.status(200).send("System error, please try again.");
		});	

	});
});

exports.getAccountInfo = functions.https.onRequest((req, res) => {
    if (req.method === 'PUT') {
		res.status(403).send('Forbidden!');
		return;
    }
    cors(req, res, () => {
        let token = req.query.token;
        var tokenData = token;
		//validation
		if (!token) {
			res.status(200).send("Please login");
			return;
		} else {
            var tokenDoc = admin.firestore().collection('tokens').doc(token);
            tokenDoc.get()
            .then(doc => {
                //if token exists then send data otherwise error response
                if (!doc.exists) {
                    console.log('Invalid token');
                    res.status(200).send("Invalid token");
                } else {
                    console.log('valid token');
                    // TODO: Add code to get information from db
                    // Lets assume we get account balance from db
                    var accountBal = '$200';
                    res.status(200).send(accountBal);
                }
            });
        }
	});
});

exports.destroySession = functions.https.onRequest((req, res) => {
    if (req.method === 'PUT') {
		res.status(403).send('Forbidden!');
		return;
    }
    cors(req, res, () => {
        let token = req.query.token;
        var tokenData = token;
		//validation
		if (!token) {
			res.status(200).send("Please login");
			return;
		} else {
            // Delete token entry from db
            var tokenDoc = admin.firestore().collection('tokens').doc(token).delete();
            tokenDoc.delete();
            res.status(200).send("Delete success");  
        }
	});
});
