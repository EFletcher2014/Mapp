Create Table mapp.units(PrimaryKey int NOT NULL AUTO_INCREMENT, 
	siteName varchar(32) NOT NULL,#figure out the correct length
	unitNumber int NOT NULL,
	levelNumber int NOT NULL,
	imageReference varchar(32),#shouldn't actually be a varchar, reference to a location on the server
	description text,
	dateTime timestamp,
	PRIMARY KEY(PrimaryKey)
);

Create Table mapp.subpictures(
	PrimaryKey int NOT NULL AUTO_INCREMENT,
	foreignKey int NOT NULL,
	imageReference varchar(32) NOT NULL,#same as above
	description text,
	/*some idea of subpicture location within the main picture, probably pixel coordinates*/
	PRIMARY KEY(PrimaryKey)
);

Insert into units(siteName, unitNumber, levelNumber, imageReference, description, dateTime)
	values('20BE23', 4, 4, 'imagereference', 'this is just a test unit/level for test purposes', '2017-06-27 02:30:33');#just populates the database with data, will actually be done in code

Select * from mapp.units #just an example for now