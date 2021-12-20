Map one = ${(typedObject.mapOfTypes(oneId).one)!'null'}
Map two = ${(typedObject.mapOfTypes(twoId).two)!'null'}
Map key/value = ${(typedObject.fullyGenericMapOfTypes(twoId).two)!'null'}
List string = ${(typedObject.listOfStrings[0])!'null'}
List two = ${(typedObject.listOfTypes[0].two)!'null'}
Single string = ${(typedObject.singleString)!'null'}
Single two = ${(typedObject.singleType.two)!'null'}

Private Map one = ${(typedObject.privateMapOfTypes(oneId).one)!'null'}
Private Map two = ${(typedObject.privateMapOfTypes(twoId).two)!'null'}
Private Map key/value = ${(typedObject.privateFullyGenericMapOfTypes(twoId).two)!'null'}
Private List string = ${(typedObject.privateListOfStrings[0])!'null'}
Private List two = ${(typedObject.privateListOfTypes[0].two)!'null'}
Private Single string = ${(typedObject.privateSingleString)!'null'}
Private Single two = ${(typedObject.privateSingleType.two)!'null'}