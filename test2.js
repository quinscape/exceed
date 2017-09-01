(function() {
  return _v.updateScope(
    ["current"],
    _domainService.create("Customer")
  ), _v.updateScope(
    ["billingAddress"],
    _domainService.create("Address")
  ), _v.updateScope(
    ["deliveryAddress"],
    _domainService.create("Address")
  ), _v.updateScope(
    ["current", "billingAddressId"],
    _v.scope(["billingAddress", "id"])
  ), _v.updateScope(
    ["hasDeliveryAddress"],
    false
  ), _a.action("newCustomerNumber", []).then(function(__current) {
    _v.updateScope(["current", "number"], __current);
  });
})();

