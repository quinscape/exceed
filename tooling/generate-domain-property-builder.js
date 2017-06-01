// script to convert a JSON definition of a domain type ito a list of DomainProperty.builder() instructions
const DOMAIN_TYPE = {
    "type": "xcd.domain.DomainType",
    "identityGUID": "8fbe7139-f89b-4ec6-acb8-c079ba3fce4f",
    "versionGUID": "118e641f-fd87-4da2-affc-b538177b7a40",
    "extension": 0,
    "description": null,
    "name": "AppTranslation",
    "pkFields": ["id"],
    "system": false,
    "properties": [
        {
            "name": "id",
            "type": "UUID",
            "required": true,
            "maxLength": 36,
            "domainType": "AppTranslation"
        }, {
            "name": "locale",
            "type": "PlainText",
            "required": true,
            "maxLength": 64,
            "domainType": "AppTranslation"
        }, {
            "name": "tag",
            "type": "PlainText",
            "required": true,
            "maxLength": 255,
            "domainType": "AppTranslation"
        }, {
            "name": "translation",
            "type": "PlainText",
            "required": true,
            "maxLength": 0,
            "domainType": "AppTranslation"
        }, {
            "name": "created",
            "type": "Timestamp",
            "required": true,
            "maxLength": 0,
            "domainType": "AppTranslation"
        }, {
            "name": "processName",
            "type": "PlainText",
            "required": false,
            "maxLength": 64,
            "domainType": "AppTranslation"
        }, {
            "name": "viewName",
            "type": "PlainText",
            "required": false,
            "maxLength": 64,
            "domainType": "AppTranslation"
        }
    ]
};

console.log(
    DOMAIN_TYPE.properties.map(propDecl =>
    {

        let s = "DomainProperty.builder().withName(" + JSON.stringify(propDecl.name) + ").withType(" + JSON.stringify(propDecl.type) + ")";

        if (propDecl.typeParam)
        {
            s += ".withTypeParam(" + JSON.stringify(propDecl.typeParam) + ")";
        }
        if (propDecl.defaultValue)
        {
            s += ".withDefaultValue(" + JSON.stringify(propDecl.defaultValue) + ")";
        }

        if (propDecl.required > 0)
        {
            s += ".setRequired(true)";
        }

        if (propDecl.maxLength > 0)
        {
            s += ".withMaxLength(" + propDecl.maxLength + ")";
        }

        if (propDecl.domainType > 0)
        {
            s += ".withDomainType(" + propDecl.domainType + ")";
        }

        return s + ".build()";

    }).join(",\n")
);
