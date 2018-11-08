Map one = ${(typedObject.mapOfTypes(oneId).one)!'null'}
Map two = ${(typedObject.mapOfTypes(twoId).two)!'null'}
Map key/value = ${(typedObject.fullyGenericMapOfTypes(twoId).two)!'null'}
List string = ${(typedObject.listOfStrings[0])!'null'}
List two = ${(typedObject.listOfTypes[0].two)!'null'}
Single string = ${(typedObject.singleString)!'null'}
Single two = ${(typedObject.singleType.two)!'null'}