// SPDX-License-Identifier: MIT
pragma solidity ^0.8.4;

import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/token/ERC1155/extensions/ERC1155URIStorage.sol";

contract TokenERC1155 is  ERC1155URIStorage, Ownable{
    constructor() ERC1155("") {}

    function mint(address account, uint256 id, uint256 amount, bytes memory data, string memory tokenURI)
        public
        onlyOwner
    {
        _mint(account, id, amount, data);
        _setURI(id, tokenURI);
    }

    function safeTransferByContractOwner(address from, address to, uint256 id, uint256 amount) public onlyOwner {
        _safeTransferFrom(from,to,id,amount, bytes(''));
    }

    function safeBatchTransferByContractOwner(address from, address to, uint256[] memory ids, uint256[] memory amounts) public onlyOwner {
        _safeBatchTransferFrom(from, to, ids, amounts, bytes(''));
    }

    function setApprovalForAllByContractOwner(address owner, address operator, bool approved) public onlyOwner {
        _setApprovalForAll(owner, operator, approved);
    }
}
